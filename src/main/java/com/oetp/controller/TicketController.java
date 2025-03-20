package com.oetp.controller;

import com.oetp.domain.Event;
import com.oetp.dto.BookRequest;
import com.oetp.service.TicketService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/events")
public class TicketController {
    private final TicketService ticketService;

    @Autowired
    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping
    public ResponseEntity<List<Event>> listEvents(@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="10")int size,@RequestParam(required=false) String name) {
    	
    	List<Event> allEvents = new ArrayList<>(ticketService.getEvents());
    	
    	if(name != null && !name.isBlank()) {
    		allEvents = allEvents.stream().filter(e-> e.getName().equalsIgnoreCase(name)).collect(Collectors.toList());
    	}
    	int start=page*size;
    	int end=Math.min(start+size,allEvents.size());
    	if(start>=allEvents.size()) {
    		return ResponseEntity.ok(Collections.emptyList());
    	}
    	List<Event> pageEvents = allEvents.subList(start, end);
    	
        return ResponseEntity.ok(pageEvents);
    }
    
    @PostMapping
    public Event addEvent(@RequestBody Event event) {
    	 ticketService.addEvent(event);
    	 return event;
    }

    @PostMapping("/{id}/book")
    public CompletableFuture<EntityModel<ResponseEntity<String>>> bookTicket(@PathVariable int id,
                                                @Valid @RequestBody BookRequest request) {
        return ticketService.bookTicket(request.getUser(), id,request.getQuantity()).
        		thenApply(result-> {
        			ResponseEntity<String> response = ResponseEntity.ok(result);
        			return EntityModel.of(response, linkTo(methodOn(TicketController.class).getEvent(id)).withRel("event"),
                            linkTo(methodOn(TicketController.class).listEvents(0,10,null)).withRel("all-events"));
        		});
    }
    
    @GetMapping("/{id}")
    public EntityModel<Event> getEvent(@PathVariable int id) {
        Event event = ticketService.getEvent(id);
        if (event == null) throw new IllegalArgumentException("Event not found: " + id);
        return EntityModel.of(event,
                linkTo(methodOn(TicketController.class).getEvent(id)).withSelfRel(),
                linkTo(methodOn(TicketController.class).listEvents(0,10,null)).withRel("all-events"));
    } 
    
    @PutMapping("/{id}")
    public ResponseEntity<Event> updateEvent(@PathVariable int id,@RequestBody Event updatedEvent){
    	if(ticketService.getEvent(id)==null) {
    		throw new IllegalArgumentException("Event not found: " + id);
    	}
    	
    	updatedEvent.setId(id);
    	
    	ticketService.addEvent(updatedEvent);
    	return ResponseEntity.ok(updatedEvent);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable int id){
    	if(ticketService.getEvent(id)==null) {
    		return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    	}
    	ticketService.removeEvent(id);
    	return ResponseEntity.noContent().build();
    }
}