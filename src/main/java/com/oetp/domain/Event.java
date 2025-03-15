package com.oetp.domain;

public class Event {
    private final int id;
    private final String name;
    private int availableTickets;

    public Event(int id, String name, int availableTickets) {
        this.id = id;
        this.name = name;
        this.availableTickets = availableTickets;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getAvailableTickets() {
        return availableTickets;
    }

    public void reduceTickets(int quantity){
        availableTickets-=quantity;
    }
}
