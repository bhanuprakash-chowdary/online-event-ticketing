package com.oetp.controller;

import com.oetp.domain.Event;
import com.oetp.dto.BookRequest;
import com.oetp.service.TicketService;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.Comparator;
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
    public ResponseEntity<List<Event>> listEvents(
    		@RequestParam(defaultValue="0") int page,
    		@RequestParam(defaultValue="10")int size,
    		@RequestParam(required=false) String name,
			@RequestParam(required = false, defaultValue = "id") String sort) {

		List<Event> allEvents = name != null && !name.isBlank() ? allEvents = ticketService.getEvents().stream()
				.filter(e -> e.getName().equalsIgnoreCase(name)).collect(Collectors.toList())
				: ticketService.getEvents();

		
		Comparator<Event>  comparator= sort.equalsIgnoreCase("name")
				? Comparator.comparing(Event::getName):Comparator.comparing(Event::getId);
		
		allEvents.sort(comparator);
		
		int start = page * size;
		int end = Math.min(start + size, allEvents.size());
		if (start >= allEvents.size()) {
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
    @RateLimiter(name = "booking")
    public CompletableFuture<EntityModel<ResponseEntity<String>>> bookTicket(@PathVariable int id,
                                                @Valid @RequestBody BookRequest request) {
        return ticketService.bookTicket(request.getUser(), id,request.getQuantity()).
        		thenApply(result-> {
        			ResponseEntity<String> response = ResponseEntity.ok(result);
        			return EntityModel.of(response, linkTo(methodOn(TicketController.class).getEvent(id)).withRel("event"),
                            linkTo(methodOn(TicketController.class).listEvents(0,10,null,"id")).withRel("all-events"));
        		});
    }
    
    @PostMapping("/batch-book")
    @RateLimiter(name = "booking")
    public CompletableFuture<ResponseEntity<String>> batchBookTickets(@Valid @RequestBody List<BookRequest> requests) {
        List<CompletableFuture<String>> futures = requests.stream()
                .map(req -> ticketService.bookTicket(req.getUser(), req.getEventId(), req.getQuantity()))
                .collect(Collectors.toList());

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    String result = futures.stream()
                            .map(CompletableFuture::join)
                            .collect(Collectors.joining(", "));
//                    logger.info("Batch booking completed: {}", result);
                    return ResponseEntity.ok("Batch booked: " + result);
                });
    }
    
    @GetMapping("/{id}")
    public EntityModel<Event> getEvent(@PathVariable int id) {
        Event event = ticketService.getEvent(id);
        if (event == null) throw new IllegalArgumentException("Event not found: " + id);
        return EntityModel.of(event,
                linkTo(methodOn(TicketController.class).getEvent(id)).withSelfRel(),
                linkTo(methodOn(TicketController.class).listEvents(0,10,null,"id")).withRel("all-events"));
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