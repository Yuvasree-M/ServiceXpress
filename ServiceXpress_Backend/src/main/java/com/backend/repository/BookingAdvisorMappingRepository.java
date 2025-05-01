package com.backend.repository;

import com.backend.model.BookingAdvisorMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookingAdvisorMappingRepository extends JpaRepository<BookingAdvisorMapping, Long> {
    Optional<BookingAdvisorMapping> findByBookingId(Long bookingId);
}