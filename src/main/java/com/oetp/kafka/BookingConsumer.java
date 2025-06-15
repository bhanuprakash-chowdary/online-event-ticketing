package com.oetp.kafka;

import com.oetp.dto.BookRequest;
import com.oetp.service.EventService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class BookingConsumer {
    private final EventService eventService;

    public BookingConsumer(EventService eventService) {
        this.eventService = eventService;
    }

    @KafkaListener(topics = "booking-queue", groupId = "oetp-bookings")
    public void processBooking(BookRequest request) {
        eventService.bookTicketSync(request.getUser(), request.getEventId(), request.getQuantity());
    }
}
