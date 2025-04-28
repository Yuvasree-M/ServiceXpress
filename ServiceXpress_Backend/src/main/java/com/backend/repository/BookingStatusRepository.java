package com.backend.repository;

import com.backend.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingStatusRepository extends JpaRepository<BookingStatus, Long> {
    BookingStatus findByBookingRequestId(Long bookingRequestId);
}
