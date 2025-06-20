package com.oetp.controller;

import com.oetp.domain.Booking;
import com.oetp.domain.Event;
import com.oetp.dto.BookRequest;
import com.oetp.service.EventService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
    @RateLimiter(name = "booking")
    public ResponseEntity<CollectionModel<EntityModel<Event>>> listEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String eventDate,
            @RequestParam(required = false, defaultValue = "id") String sort) {
        Pageable pageable = PageRequest.of(page, size,
                sort.equalsIgnoreCase("name") ? Sort.by("name") : Sort.by("id"));
        LocalDateTime date = eventDate != null ? LocalDateTime.parse(eventDate) : null;
        Page<Event> eventPage = eventService.findByFilters(name, category, location, date, pageable);

        List<EntityModel<Event>> eventModels = eventPage.getContent().stream()
                .map(event -> EntityModel.of(event,
                        linkTo(methodOn(EventController.class).getEvent(event.getId())).withSelfRel(),
                        linkTo(methodOn(EventController.class).listEvents(page, size, name, category, location, eventDate, sort)).withRel("events")))
                .collect(Collectors.toList());

        CollectionModel<EntityModel<Event>> collectionModel = CollectionModel.of(eventModels);
        collectionModel.add(linkTo(methodOn(EventController.class).listEvents(page, size, name, category, location, eventDate, sort)).withSelfRel());
        if (eventPage.hasNext()) {
            collectionModel.add(linkTo(methodOn(EventController.class)
                    .listEvents(page + 1, size, name, category, location, eventDate, sort)).withRel("next"));
        }
        if (eventPage.hasPrevious()) {
            collectionModel.add(linkTo(methodOn(EventController.class)
                    .listEvents(page - 1, size, name, category, location, eventDate, sort)).withRel("prev"));
        }

        return ResponseEntity.ok(collectionModel);
    }

    @PostMapping
    public Event addEvent(@RequestBody Event event) {
        eventService.addEvent(event);
        return event;
    }

    @PostMapping("/{id}/book")
    @RateLimiter(name = "booking")
    public CompletableFuture<EntityModel<Booking>> bookTicket(@PathVariable int id,
                                                              @Valid @RequestBody BookRequest request) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return CompletableFuture.supplyAsync(() -> {
            Booking booking = eventService.createBooking(userEmail, id, request.getQuantity());
            return EntityModel.of(booking,
                    linkTo(methodOn(EventController.class).getEvent(id)).withRel("event"),
                    linkTo(methodOn(EventController.class).listEvents(0, 10, null, null, null, null, "id")).withRel("all-events"));
        });
    }

    @PostMapping("/{id}/v1/book")
    @RateLimiter(name = "booking")
    public CompletableFuture<String> bookTicketV1(@PathVariable int id,
                                                              @Valid @RequestBody BookRequest request) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return eventService.bookTicket(userEmail, id, request.getQuantity());
    }

    @PostMapping("/batch-book")
    @RateLimiter(name = "booking")
    public CompletableFuture<ResponseEntity<String>> batchBookTickets(@Valid @RequestBody List<BookRequest> requests) {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        List<CompletableFuture<Booking>> futures = requests.stream()
                .map(req -> CompletableFuture.supplyAsync(() ->
                        eventService.createBooking(userEmail, req.getEventId(), req.getQuantity())))
                .toList();

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    String result = futures.stream()
                            .map(CompletableFuture::join)
                            .map(booking -> String.format("%s booked %d for %s",
                                    booking.getUserEmail(), booking.getQuantity(), booking.getEvent().getName()))
                            .collect(Collectors.joining(", "));
                    return ResponseEntity.ok("Batch booked: " + result);
                });
    }

    @GetMapping("/{id}")
    public EntityModel<Event> getEvent(@PathVariable int id) {
        Event event = eventService.getEvent(id);
        if (event == null) throw new IllegalArgumentException("Event not found: " + id);
        return EntityModel.of(event,
                linkTo(methodOn(EventController.class).getEvent(id)).withSelfRel(),
                linkTo(methodOn(EventController.class).listEvents(0, 10, null, null, null, null, "id")).withRel("all-events"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Event> updateEvent(@PathVariable int id, @RequestBody Event updatedEvent) {
        if (eventService.getEvent(id) == null) {
            throw new IllegalArgumentException("Event not found: " + id);
        }
        updatedEvent.setId(id);
        eventService.addEvent(updatedEvent);
        return ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable int id) {
        if (eventService.getEvent(id) == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        eventService.removeEvent(id);
        return ResponseEntity.noContent().build();
    }
}