package com.oetp.service;

import com.oetp.domain.Event;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class TicketService {

    private final ConcurrentHashMap<Integer, Event> events;
    private final ReentrantLock lock=new ReentrantLock();

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

    public void bookTicket(String user,int eventId,int quantity) { //synchronize locks whole function
        Event event = events.get(eventId);
        System.out.println(user + " checking " + quantity + " for " + event.getName() + "( Available at the moment " + event.getAvailableTickets() + ")");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (event.reduceTickets(quantity)) {
            System.out.println(user + " booked " + quantity + ". Remaining: " + event.getAvailableTickets());
        } else {
            System.out.println(user + " failed - not enough tickets.");
        }

//            lock.lock();//lock only reduce Tickets (not whole method including sleep )
//            try {
//                if (event.getAvailableTickets() >= quantity) {
//                    event.reduceTickets(quantity);
//                    System.out.println(user + " booked " + quantity + ". Remaining: " + event.getAvailableTickets());
//                } else {
//                    System.out.println(user + " failed - not enough tickets (race detected).");
//                }
//            } finally {
//                lock.unlock();
//            }
    }
}
