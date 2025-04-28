package com.backend.service;

import com.backend.model.BookingRequest;
import com.backend.model.BookingStatus;
import com.backend.repository.BookingRequestRepository;
import com.backend.repository.BookingStatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class BookingService {

    @Autowired
    private BookingRequestRepository bookingRequestRepository;

    @Autowired
    private BookingStatusRepository bookingStatusRepository;

    public void assignAdvisor(Long bookingId, String advisorUsername) {
        BookingRequest br = bookingRequestRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!br.getStatus().equals("PENDING")) {
            throw new RuntimeException("Booking is not in PENDING status");
        }
        br.setStatus("IN_PROGRESS");
        br.setUpdatedAt(LocalDate.now());
        bookingRequestRepository.save(br);

        BookingStatus bs = bookingStatusRepository.findByBookingRequestId(bookingId);
        if (bs == null) {
            bs = BookingStatus.builder()
                    .bookingRequest(br)
                    .serviceAdvisor(advisorUsername)
                    .build();
        } else {
            bs.setServiceAdvisor(advisorUsername);
        }
        bookingStatusRepository.save(bs);
    }

    public void completeBooking(Long bookingId, String billOfMaterials) {
        BookingRequest br = bookingRequestRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!br.getStatus().equals("IN_PROGRESS")) {
            throw new RuntimeException("Booking is not in IN_PROGRESS status");
        }
        br.setStatus("COMPLETED");
        br.setUpdatedAt(LocalDate.now());
        bookingRequestRepository.save(br);

        BookingStatus bs = bookingStatusRepository.findByBookingRequestId(bookingId);
        if (bs == null) {
            bs = BookingStatus.builder()
                    .bookingRequest(br)
                    .build();
        }
        bs.setCompletedDate(LocalDate.now());
        bs.setBillOfMaterials(billOfMaterials);
        bs.setPaymentRequested(true);
        bookingStatusRepository.save(bs);
    }
}