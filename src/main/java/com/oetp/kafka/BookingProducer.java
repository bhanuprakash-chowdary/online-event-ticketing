package com.oetp.kafka;

import com.oetp.dto.BookRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class BookingProducer {
    private final KafkaTemplate<String, BookRequest> kafkaTemplate;

    public BookingProducer(KafkaTemplate<String, BookRequest> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendBooking(String user, int eventId, int quantity) {
        BookRequest request = new BookRequest(user, eventId, quantity);
        kafkaTemplate.send("booking-queue", String.valueOf(eventId), request);
    }
}
