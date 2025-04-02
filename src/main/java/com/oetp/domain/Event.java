package com.oetp.domain;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Event {
	
	@Id
    private int id;
    private String name;
    private int availableTickets;

    public Event() {}
    public Event(int id, String name, int availableTickets) {
        this.id = id;
        this.name = name;
        this.availableTickets = availableTickets;
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
//        return name != null ? name.hashCode() : 0;
        int result = id; // Simple base
        result = 31 * result + (name != null ? name.hashCode() : 0); // Mix in name
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if(this==obj){
            return true;
        }

        if(! (obj instanceof Event e)){
            return false;
        }

        return id==e.id && (Objects.equals(name, e.name));
    }
}
