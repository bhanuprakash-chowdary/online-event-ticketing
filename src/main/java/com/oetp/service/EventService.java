package com.oetp.service;

import com.oetp.domain.Booking;
import com.oetp.domain.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface EventService {

    public void addEvent(Event event);

    public Event getEvent(int id);

    public List<Event> getEvents();

    public CompletableFuture<String> bookTicket(String user, int id, int quantity);

	public void removeEvent(int id);

    Page<Event> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Event> findAll(Pageable pageable);

    Booking createBooking(String userEmail, int eventId, int quantity);

    void bookTicketSync(String userEmail, int eventId, int quantity);

    Page<Event> findByCategory(String category, Pageable pageable);

    Page<Event> findByLocation(String location, Pageable pageable);

    Page<Event> findByEventDateAfter(LocalDateTime date, Pageable pageable);

    Page<Event> findByFilters(String name, String category, String location, LocalDateTime date, Pageable pageable);

}
