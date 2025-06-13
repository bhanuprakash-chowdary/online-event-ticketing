package com.oetp.dao;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.oetp.domain.Event;

public interface TicketRepository extends JpaRepository<Event, Integer> {
    List<Event> findByNameContainingIgnoreCase(String name);
}
