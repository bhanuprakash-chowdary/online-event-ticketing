package com.oetp.controller;

import com.oetp.domain.Event;
import com.oetp.service.TicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/events")
public class TicketController {
    private final TicketService ticketService;

    @Autowired
    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public Collection<Event> listEvents() {
        return ticketService.getEvents();
    }

    @PostMapping("/{id}/book")
    public CompletableFuture<String> bookTicket(@PathVariable int id,
                                                @RequestParam String user,
                                                @RequestParam int quantity) {
        return ticketService.bookTicket(user, id, quantity);
    }
}