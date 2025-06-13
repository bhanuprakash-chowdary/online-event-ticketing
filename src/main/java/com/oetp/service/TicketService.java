package com.oetp.service;

import com.oetp.domain.Event;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface TicketService {

    public void addEvent(Event event);

    public Event getEvent(int id);

    public List<Event> getEvents();

    public CompletableFuture<String> bookTicket(String user, int id, int quantity);

	public void removeEvent(int id);
}
