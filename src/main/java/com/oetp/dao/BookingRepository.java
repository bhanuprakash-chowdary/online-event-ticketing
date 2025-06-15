package com.oetp.dao;

import com.oetp.domain.Booking;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    boolean existsByUserEmailAndEventId(String userEmail, int eventId);
}