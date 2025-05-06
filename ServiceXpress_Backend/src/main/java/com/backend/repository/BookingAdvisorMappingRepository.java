package com.backend.repository;

import com.backend.model.BookingAdvisorMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookingAdvisorMappingRepository extends JpaRepository<BookingAdvisorMapping, Long> {
    Optional<BookingAdvisorMapping> findByBookingId(Long bookingId);
    List<BookingAdvisorMapping> findByAdvisorId(Long advisorId);
}