package com.oetp.service;

import com.oetp.domain.Event;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class TicketService {

    private final ConcurrentHashMap<Integer, Event> events;
    private final Semaphore semaphore=new Semaphore(5);
    private final CyclicBarrier barrier = new CyclicBarrier(5,()->System.out.println("Batch of 5 bookings processed!"));
    private final AtomicInteger batchCounter = new AtomicInteger(0);
    //private final ReentrantLock lock=new ReentrantLock();

    public TicketService(){
        this.events=new ConcurrentHashMap<>();
    }

    public void addEvent(Event event){
        events.put(event.getId(),event);
    }

    public void listEvents() {
        if (events.isEmpty()) {
            System.out.println("No events available.");
        } else {
            events.forEach((id, event) ->
                    System.out.println("ID: " + id + ", Name: " + event.getName() +
                            ", Tickets: " + event.getAvailableTickets()));
        }
    }

    public Event getEvent(int id) {
        return events.get(id);
    }

    public String bookTicket(String user, int eventId, int quantity) { //synchronize locks whole function

//        return CompletableFuture.supplyAsync(()-> {
            try {
                barrier.await();
                batchCounter.incrementAndGet();
                semaphore.acquire();//grab a permit (wait if none).
                Event event = events.get(eventId);
                if (event != null) {
                    if (event.getAvailableTickets() >= quantity) {

                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }

                        if (event.reduceTickets(quantity)) {
                            return user + " booked " + quantity + " tickets for " + event.getName() +
                                    ". Remaining: " + event.getAvailableTickets();
                        }
                    }

                    return user + " failed - not enough tickets for " + event.getName();
                }
                return "Event not found: " + eventId;
            } catch (Exception e) {
                Thread.currentThread().interrupt();
                return user + " booking interrupted";
            }finally {
                semaphore.release();// Free permit
            }
//        });
    }
}
