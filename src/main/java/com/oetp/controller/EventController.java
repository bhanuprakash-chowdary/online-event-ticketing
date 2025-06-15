package com.oetp.controller;

import com.oetp.domain.Event;
import com.oetp.dto.BookRequest;
import com.oetp.service.EventService;

import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@RestController
@RequestMapping("/v1/events")
public class EventController {
    private final EventService eventService;

    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    @Cacheable(value = "eventCache", key = "'allEvents'")
    public ResponseEntity<List<Event>> listEvents(
    		@RequestParam(defaultValue="0") int page,
    		@RequestParam(defaultValue="10")int size,
    		@RequestParam(required=false) String name,
			@RequestParam(required = false, defaultValue = "id") String sort) {

		List<Event> allEvents = name != null && !name.isBlank() ? allEvents = eventService.getEvents().stream()
				.filter(e -> e.getName().equalsIgnoreCase(name)).collect(Collectors.toList())
				: eventService.getEvents();


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

//    @GetMapping
//    @RateLimiter(name = "eventsList")
//    public ResponseEntity<CollectionModel<EntityModel<Event>>> listEvents(
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            @RequestParam(required = false) String name,
//            @RequestParam(required = false, defaultValue = "id") String sort) {
//        Pageable pageable = PageRequest.of(page, size,
//                sort.equalsIgnoreCase("name") ? Sort.by("name") : Sort.by("id"));
//        Page<Event> eventPage = name != null && !name.isBlank()
//                ? ticketRepository.findByNameContainingIgnoreCase(name, pageable)
//                : ticketRepository.findAll(pageable);
//
//        List<EntityModel<Event>> eventModels = eventPage.getContent().stream()
//                .map(event -> EntityModel.of(event,
//                        linkTo(methodOn(TicketController.class).getEvent(event.getId())).withSelfRel(),
//                        linkTo(methodOn(TicketController.class).listEvents(page, size, name, sort)).withRel("events")))
//                .collect(Collectors.toList());
//
//        CollectionModel<EntityModel<Event>> collectionModel = CollectionModel.of(eventModels);
//        collectionModel.add(linkTo(methodOn(TicketController.class).listEvents(page, size, name, sort)).withSelfRel());
//        if (eventPage.hasNext()) {
//            collectionModel.add(linkTo(methodOn(TicketController.class)
//                    .listEvents(page + 1, size, name, sort)).withRel("next"));
//        }
//        if (eventPage.hasPrevious()) {
//            collectionModel.add(linkTo(methodOn(TicketController.class)
//                    .listEvents(page - 1, size, name, sort)).withRel("prev"));
//        }
//
//        return ResponseEntity.ok(collectionModel);
//    }



    @PostMapping
    public Event addEvent(@RequestBody Event event) {
    	 eventService.addEvent(event);
    	 return event;
    }

    @PostMapping("/{id}/book")
    @RateLimiter(name = "booking")
    public CompletableFuture<EntityModel<ResponseEntity<String>>> bookTicket(@PathVariable int id,
                                                @Valid @RequestBody BookRequest request
                                               //@AuthenticationPrincipal OAuth2User principal
                                                                             ) {

//        String user = principal.getAttribute("email");
        return eventService.bookTicket(request.getUser(), id,request.getQuantity()).
        		thenApply(result-> {
        			ResponseEntity<String> response = ResponseEntity.ok(result);
        			return EntityModel.of(response, linkTo(methodOn(EventController.class).getEvent(id)).withRel("event"),
                            linkTo(methodOn(EventController.class).listEvents(0,10,null,"id")).withRel("all-events"));
        		});
    }
    
    @PostMapping("/batch-book")
    @RateLimiter(name = "booking")
    public CompletableFuture<ResponseEntity<String>> batchBookTickets(@Valid @RequestBody List<BookRequest> requests) {
        List<CompletableFuture<String>> futures = requests.stream()
                .map(req -> eventService.bookTicket(req.getUser(), req.getEventId(), req.getQuantity()))
                .toList();

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
        Event event = eventService.getEvent(id);
        if (event == null) throw new IllegalArgumentException("Event not found: " + id);
        return EntityModel.of(event,
                linkTo(methodOn(EventController.class).getEvent(id)).withSelfRel(),
                linkTo(methodOn(EventController.class).listEvents(0,10,null,"id")).withRel("all-events"));
    } 
    
    @PutMapping("/{id}")
    public ResponseEntity<Event> updateEvent(@PathVariable int id,@RequestBody Event updatedEvent){
    	if(eventService.getEvent(id)==null) {
    		throw new IllegalArgumentException("Event not found: " + id);
    	}
    	
    	updatedEvent.setId(id);
    	
    	eventService.addEvent(updatedEvent);
    	return ResponseEntity.ok(updatedEvent);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable int id){
    	if(eventService.getEvent(id)==null) {
    		return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    	}
    	eventService.removeEvent(id);
    	return ResponseEntity.noContent().build();
    }
}