package com.backend.controller;

import com.backend.dto.BookingResponseDTO;
import com.backend.dto.DashboardDataDTO;
import com.backend.model.Advisor;
import com.backend.model.BookingRequest;
import com.backend.service.BookingRequestService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:8082")
@RequestMapping("/api/admin")
public class AdminBookingController {

    private final BookingRequestService bookingService;

    @Autowired
    public AdminBookingController(BookingRequestService bookingService) {
        this.bookingService = bookingService;
    }

    @GetMapping("/bookings/pending")
    public ResponseEntity<List<BookingResponseDTO>> getPendingBookings() {
        System.out.println("Fetching pending bookings");
        List<BookingResponseDTO> pendingBookings = bookingService.getPendingBookings();
        return pendingBookings.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(pendingBookings);
    }

    @PostMapping("/bookings/{bookingId}/assign-advisor")
    public ResponseEntity<BookingRequest> assignServiceAdvisor(@PathVariable Long bookingId, @RequestBody Long advisorId) {
        try {
            BookingRequest updatedBooking = bookingService.assignServiceAdvisor(bookingId, advisorId);
            return ResponseEntity.ok(updatedBooking);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
    


    @PostMapping("/bookings/{bookingId}/mark-completed")
    public ResponseEntity<BookingRequest> markBookingCompleted(@PathVariable Long bookingId) {
        try {
            BookingRequest updatedBooking = bookingService.markBookingCompleted(bookingId);
            return ResponseEntity.ok(updatedBooking);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }
}