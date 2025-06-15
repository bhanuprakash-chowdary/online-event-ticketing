package com.oetp.serviceimpl;

import com.oetp.dao.BookingRepository;
import com.oetp.dao.EventRepository;
import com.oetp.domain.Booking;
import com.oetp.domain.Event;
import com.oetp.kafka.BookingProducer;
import com.oetp.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventServiceImpl implements EventService {

	private static final Logger logger = LoggerFactory.getLogger(EventServiceImpl.class);
	private final EventRepository eventRepository;
    private final Semaphore semaphore;
    private final AtomicInteger batchCounter = new AtomicInteger(0);
	private final Executor executor;
    private final BookingRepository bookingRepository;
    private final BookingProducer bookingProducer;


    @Autowired
    public EventServiceImpl(EventRepository repository, BookingRepository bookingRepository,
                            @Qualifier("bookingExecutor") Executor executor, Semaphore semaphore,BookingProducer bookingProducer) {
    	this.eventRepository=repository;
        this.bookingRepository = bookingRepository;
    	this.executor = executor;
        this.semaphore = semaphore;
        this.bookingProducer=bookingProducer;
    }


    @Async("bookingExecutor")
    public CompletableFuture<String> bookTicket(String user, int eventId, int quantity) {
        bookingProducer.sendBooking(user, eventId, quantity);
        return CompletableFuture.completedFuture("Booking queued for " + user);
    }

    @Override
    @Async
    @Transactional// Vaults/ropes/locksResources
//    @CacheEvict(value = "eventCache", key = "#eventId")
    public CompletableFuture<String> bookTicketSync(String user, int eventId, int quantity) {
    	return CompletableFuture.supplyAsync(() -> {
            try {
                semaphore.acquire();
                Booking booking = createBooking(user, eventId, quantity);
                String result = String.format("%s booked %d for %s", user, quantity, booking.getEvent().getName());
                logger.info("Booking success: {}", result);
                int count = batchCounter.incrementAndGet();
                if (count % 5 == 0) {
                    logger.info("Batch of 5 bookings processed! Total: {}", count);
                }
                return result;
            } catch (InterruptedException e) {
                logger.error("Booking interrupted for user: {}", user, e);
                Thread.currentThread().interrupt();
                throw new RuntimeException("Booking interrupted");
            } finally {
                semaphore.release();
            }
        }, executor);
    }

    @Override
    @Transactional
    public Booking createBooking(String userEmail, int eventId, int quantity) {
        if (bookingRepository.existsByUserEmailAndEventId(userEmail, eventId)) {
            throw new IllegalStateException("User has already booked this event");
        }
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));
        if (!event.reduceTickets(quantity)) {
            throw new IllegalStateException("Not enough tickets");
        }
        eventRepository.save(event);
        Booking booking = new Booking(userEmail, event, quantity, LocalDateTime.now());
        return bookingRepository.save(booking);
    }

    @Override
    public void addEvent(Event event) {
        eventRepository.save(event);
    }

    @Override
//    @Cacheable(value = "eventCache", key = "#id")
    public Event getEvent(int id) {
    	logger.info("Fetching event {} from DB", id);
        return eventRepository.findById(id).orElse(null);
    }

    @Override
//    @Cacheable(value = "eventCache", key = "'allEvents'")
    public List<Event> getEvents(){
        logger.info("Fetching events from DB");
        return eventRepository.findAll();
    }

    @Override
    public void removeEvent(int id) {
    	eventRepository.deleteById(id);
    }

    @Override
    public Page<Event> findByNameContainingIgnoreCase(String name, Pageable pageable) {
        logger.info("Fetching events with name containing: {}", name);
        return eventRepository.findByNameContainingIgnoreCase(name, pageable);
    }

    @Override
    public Page<Event> findAll(Pageable pageable) {
        logger.info("Fetching all events with pagination");
        return eventRepository.findAll(pageable);
    }

    @Override
    public Page<Event> findByCategory(String category, Pageable pageable) {
        logger.info("Fetching events with category: {}", category);
        return eventRepository.findByCategory(category, pageable);
    }

    @Override
    public Page<Event> findByLocation(String location, Pageable pageable) {
        logger.info("Fetching events with location: {}", location);
        return eventRepository.findByLocation(location, pageable);
    }

    @Override
    public Page<Event> findByEventDateAfter(LocalDateTime date, Pageable pageable) {
        logger.info("Fetching events after date: {}", date);
        return eventRepository.findByEventDateAfter(date, pageable);
    }

    @Override
    public Page<Event> findByFilters(String name, String category, String location, LocalDateTime date, Pageable pageable) {
        logger.info("Fetching events with filters - name: {}, category: {}, location: {}, date: {}", name, category, location, date);
        if (name == null && category == null && location == null && date == null) {
            return eventRepository.findAll(pageable);
        }
        return eventRepository.findByNameContainingIgnoreCaseAndCategoryAndLocationAndEventDateAfter(
                name != null ? name : "",
                category != null ? category : "",
                location != null ? location : "",
                date != null ? date : LocalDateTime.of(1970, 1, 1, 0, 0),
                pageable);
    }

}
