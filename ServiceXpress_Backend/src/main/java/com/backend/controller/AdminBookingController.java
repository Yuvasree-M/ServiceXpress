package com.backend.controller;

import com.backend.dto.BookingResponseDTO;
import com.backend.model.Advisor;
import com.backend.model.BookingRequest;
import com.backend.service.BookingRequestService;

import org.apache.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:8082")
@RequestMapping("/api/admin/bookings")
public class AdminBookingController {

    private final BookingRequestService bookingService;

    @Autowired
    public AdminBookingController(BookingRequestService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/pending")
    public ResponseEntity<List<BookingResponseDTO>> getPendingBookings() {
        System.out.println("Fetching pending bookings");
        List<BookingResponseDTO> pendingBookings = bookingService.getPendingBookings();
        return pendingBookings.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(pendingBookings);
    }

    @PostMapping("/{bookingId}/assign-advisor")
    public ResponseEntity<BookingRequest> assignServiceAdvisor(@PathVariable Long bookingId, @RequestBody Long advisorId) {
        try {
            BookingRequest updatedBooking = bookingService.assignServiceAdvisor(bookingId, advisorId);
            return ResponseEntity.ok(updatedBooking);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    

    @GetMapping("/advisors")
    public ResponseEntity<List<Advisor>> getAllAdvisors() {
        System.out.println("Fetching all advisors");
        List<Advisor> advisors = bookingService.getAllAdvisors();
        return advisors.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(advisors);
    }
}