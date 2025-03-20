package com.oetp.service;

import com.oetp.domain.Event;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public interface TicketService {

    public void addEvent(Event event);

    public Event getEvent(int id);

    public Collection<Event> getEvents();

    public CompletableFuture<String> bookTicket(String user, int id, int quantity);

	public void removeEvent(int id);
}
