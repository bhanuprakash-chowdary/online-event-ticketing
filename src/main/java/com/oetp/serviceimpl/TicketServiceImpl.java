package com.oetp.serviceimpl;

import com.oetp.domain.Event;
import com.oetp.service.TicketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketServiceImpl implements TicketService {
	
	private static final Logger logger = LoggerFactory.getLogger(TicketServiceImpl.class);
    private final ConcurrentHashMap<Integer, Event> events = new ConcurrentHashMap<>();
    private final Semaphore semaphore;
    private final AtomicInteger batchCounter = new AtomicInteger(0);
	private Executor executor;
    
    
    @Autowired
    public TicketServiceImpl(@Qualifier("bookingExecutor") Executor executor, Semaphore semaphore) {
    	this.executor = executor;
        this.semaphore = semaphore;
    }
    
    @Override
    public void addEvent(Event event) {
        events.put(event.getId(), event);
    }

    @Override
    public Event getEvent(int id) {
        return events.get(id);
    }

    @Override
    @Async
    @Transactional// Vaults/ropes/locksResources
    public CompletableFuture<String> bookTicket(String user, int eventId, int quantity) {
    	return CompletableFuture.supplyAsync(() -> {
            try {
                semaphore.acquire();
                Event event = events.get(eventId);
                if (event == null) throw new IllegalArgumentException("Event not found: " + eventId);
                Thread.sleep(1000);
                if (!event.reduceTickets(quantity)) throw new IllegalStateException("Not enough tickets");
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
    public Collection<Event> getEvents(){
        return events.values();
    }
    
    @Override
    public void removeEvent(int id) {
    	if(events.remove(id)==null) {
    		throw new IllegalArgumentException("Event not found: " + id);
    	}
    }
}
