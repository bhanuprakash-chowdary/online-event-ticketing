package com.oetp.serviceimpl;

import com.oetp.dao.EventRepository;
import com.oetp.domain.Event;
import com.oetp.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventServiceImpl implements EventService {

	private static final Logger logger = LoggerFactory.getLogger(EventServiceImpl.class);
	private final EventRepository ticketRepository;
    private final Semaphore semaphore;
    private final AtomicInteger batchCounter = new AtomicInteger(0);
	private final Executor executor;


    @Autowired
    public EventServiceImpl(EventRepository repository, @Qualifier("bookingExecutor") Executor executor, Semaphore semaphore) {
    	ticketRepository=repository;
    	this.executor = executor;
        this.semaphore = semaphore;
    }

    @Override
    @Async
    @Transactional// Vaults/ropes/locksResources
    @CacheEvict(value = "eventCache", key = "#eventId")
    public CompletableFuture<String> bookTicket(String user, int eventId, int quantity) {
    	return CompletableFuture.supplyAsync(() -> {
            try {
                semaphore.acquire();
                Event event = ticketRepository.findById(eventId).orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));
                if (!event.reduceTickets(quantity)) throw new IllegalStateException("Not enough tickets");
                ticketRepository.save(event);

                String result = user + " booked " + quantity + " for " + event.getName();
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
    public void addEvent(Event event) {
        ticketRepository.save(event);
    }

    @Override
    @Cacheable(value = "eventCache", key = "#id")
    public Event getEvent(int id) {
    	logger.info("Fetching event {} from DB", id);
        return ticketRepository.findById(id).orElse(null);
    }

    @Override
    @Cacheable(value = "eventCache", key = "'allEvents'")
    public List<Event> getEvents(){
        logger.info("Fetching events from DB");
        return ticketRepository.findAll();
    }

    @Override
    public void removeEvent(int id) {
    	ticketRepository.deleteById(id);
    }
}
