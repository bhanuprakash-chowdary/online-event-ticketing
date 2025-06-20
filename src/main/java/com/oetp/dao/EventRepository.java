package com.oetp.dao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.oetp.domain.Event;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, Integer> {
    Page<Event> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<Event> findByCategory(String category, Pageable pageable);
    Page<Event> findByLocation(String location, Pageable pageable);
    Page<Event> findByEventDateAfter(LocalDateTime date, Pageable pageable);
    Page<Event> findByNameContainingIgnoreCaseAndCategoryAndLocationAndEventDateAfter(
                String name, String category, String location, LocalDateTime date, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.id = :id")
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Event> findByIdForUpdate(@Param("id") int id);

}
