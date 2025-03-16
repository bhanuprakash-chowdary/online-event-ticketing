package com.oetp.serviceimpl;

import com.oetp.domain.Event;
import com.oetp.service.TicketService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TicketServiceImpl implements TicketService {
    private final ConcurrentHashMap<Integer, Event> events = new ConcurrentHashMap<>();

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






       return CompletableFuture.supplyAsync(()-> {

            Event event = events.get(eventId);
            if (event != null) {
                if (event.reduceTickets(quantity)) {
                    return user + " booked " + quantity + " for " + event.getName();
                }
                return user + " failed - not enough tickets";
            }
            return "Event not found: " + eventId;
        });
    }

    @Override
    public Collection<Event> getEvents(){
        return events.values();
    }
}
