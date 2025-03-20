package com.oetp.serviceimpl;

import com.oetp.domain.Event;
import com.oetp.service.TicketService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.Collection;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class TicketServiceImpl implements TicketService {
	
	private static final Logger logger = LoggerFactory.getLogger(TicketServiceImpl.class);
    private final ConcurrentHashMap<Integer, Event> events = new ConcurrentHashMap<>();
    private final ExecutorService executor;
    private final Semaphore semaphore;
    
    private final CyclicBarrier barrier = new CyclicBarrier(5, () -> 
    System.out.println("Batch of 5 bookings processed!"));
    
    private final AtomicInteger batchCounter = new AtomicInteger(0);
    
    private final ThreadLocal<String> currentUser = new ThreadLocal<>();
    
    @Autowired
    public TicketServiceImpl(ExecutorService executor, Semaphore semaphore) {
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
	public CompletableFuture<String> bookTicket(String user, int eventId, int quantity) {
    	currentUser.set(user);
		return CompletableFuture.supplyAsync(() -> {
			try {
				semaphore.acquire();
				Event event = events.get(eventId);
				if (event == null)
					throw new IllegalArgumentException("Event not found: " + eventId);
				if (!event.reduceTickets(quantity))
					throw new IllegalStateException("Not enough tickets");
				
				barrier.await();
				
				String result = currentUser.get() + " booked " + quantity + " for " + event.getName();
				int count = batchCounter.incrementAndGet();
				logger.info("Booking success: {}", result);
                if (count % 5 == 0) {
                	logger.info("Batch of 5 bookings processed! Total: {}", count);
                }
                
				return result;

			} catch (InterruptedException | BrokenBarrierException e) {
				logger.error("Booking interrupted for user: {}", currentUser.get(), e);
				Thread.currentThread().interrupt();
				throw new RuntimeException("Booking interrupted");
			} finally {
				semaphore.release();
				currentUser.remove();
			}

		},executor);//run on executor pool(defined), not commonPool.
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
