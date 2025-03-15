package com.oetp.domain;

import java.util.concurrent.atomic.AtomicInteger;

public class Event {
    private final int id;
    private final String name;
    private AtomicInteger availableTickets;

    public Event(int id, String name, int availableTickets) {
        this.id = id;
        this.name = name;
        this.availableTickets = new AtomicInteger(availableTickets);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAvailableTickets() {
        return availableTickets.get();
    }

    public boolean reduceTickets(int quantity){
        int current;
        do{
            current = availableTickets.get();
            if(current<quantity){
                return false;
            }
        }while(!availableTickets.compareAndSet(current,current-quantity));
        return true;
    }
}
