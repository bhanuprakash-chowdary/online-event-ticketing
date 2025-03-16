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
    private final AtomicInteger queuedBookings = new AtomicInteger(0);
    private final AtomicInteger completedBookings = new AtomicInteger(0);
    //private final ReentrantLock lock=new ReentrantLock();

    public TicketService(){
        this.events=new ConcurrentHashMap<>();
    }

    public boolean addEvent(Event event){
        if(events.containsKey(event.getId())){
            System.out.println("Error: Event ID " + event.getId() + " already exists");
            return false;
        }
        events.put(event.getId(),event);
        return true;
    }

    public void listEvents() {
        if (events.isEmpty()) {
            System.out.println("No events available.");
        } else {
            events.values().stream().
                    filter(e-> e.getAvailableTickets()>0).
                    forEach(event -> System.out.println("ID: " + event.getId() + ", Name: " + event.getName() +
                            ", Tickets: " + event.getAvailableTickets()));
        }
    }

    public Event getEvent(int id) {
        return events.get(id);
    }

    public CompletableFuture<String> bookTicket(String user, int eventId, int quantity) { //synchronize locks whole function
        queuedBookings.incrementAndGet();
        return CompletableFuture.supplyAsync(()-> {
            try {
                semaphore.acquire();//grab a permit (wait if none).
                Event event = events.get(eventId);
                if (event != null) {
                    if (event.getAvailableTickets() >= quantity) {

                        try {
                            Thread.sleep(100);
                            barrier.await();
                            batchCounter.incrementAndGet();
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
                semaphore.release();
                completedBookings.incrementAndGet();// Free permit
            }
        });
    }

    public Event findEventByName(String name) {
        return events.values().stream()
                .filter(event -> event.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public boolean removeEvent(int id) {
        return events.remove(id) != null;
    }

    public int getBatchCount() { return batchCounter.get(); }
    public int getQueuedBookings() { return queuedBookings.get(); }
    public int getCompletedBookings() { return completedBookings.get(); }
}
