package com.oetp.domain;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import jakarta.persistence.*;
import lombok.Data;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Entity
@Table(name = "event")
@Data
public class Event implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @SequenceGenerator(name = "event_seq", sequenceName = "event_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "event_seq")
    private int id;

    private String name;

    @Column(name = "available_tickets")
    private int availableTickets;

    @Column(name = "event_date")
    private LocalDateTime eventDate;

    private String location;

    private String category;

    public Event() {}

    public Event(int id, String name, int availableTickets, LocalDateTime eventDate, String location, String category) {
        this.id = id;
        this.name = name;
        this.availableTickets = availableTickets;
        this.eventDate = eventDate;
        this.location = location;
        this.category = category;
    }

    public boolean reduceTickets(int quantity) {
        if (availableTickets >= quantity) {
            availableTickets -= quantity;
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Event e)) return false;
        return id == e.id && Objects.equals(name, e.name);
    }
}