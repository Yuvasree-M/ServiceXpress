package com.backend.repository;

import com.backend.model.BookingRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRequestRepository extends JpaRepository<BookingRequest, Long> {
    List<BookingRequest> findByCustomerId(Long customerId);
    List<BookingRequest> findByStatus(String status);
    List<BookingRequest> findByStatusIgnoreCase(String status);
    List<BookingRequest> findByIdInAndStatus(List<Long> ids, String status);
}