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

    @Override
    public int hashCode() {
//        return name != null ? name.hashCode() : 0;
        int result = id; // Simple base
        result = 31 * result + (name != null ? name.hashCode() : 0); // Mix in name
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if(this==obj){
            return true;
        }

        if(! (obj instanceof Event)){
            return false;
        }

        Event e=(Event)obj;

        return id==e.id && (name!=null?name.equals(e.name):e.name==null);
    }

    // Add getters for JSON (Spring needs them)
    public int getAvailableTicketsValue() { return availableTickets.get(); }
}
