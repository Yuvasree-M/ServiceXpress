package com.backend.controller;

import com.backend.model.BookingRequest;
import com.backend.service.BookingRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:8082")
@RequestMapping("/api/bookings")
public class BookingRequestController {

    private final BookingRequestService bookingService;

    public BookingRequestController(BookingRequestService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingRequest> createBooking(@RequestBody BookingRequest booking) {
        try {
            BookingRequest savedBooking = bookingService.createBooking(booking);
            return ResponseEntity.status(201).body(savedBooking); // 201 Created
        } catch (Exception e) {
            // Log the error and return an appropriate response
            System.err.println("Error creating booking: " + e.getMessage());
            return ResponseEntity.status(500).body(null); // 500 Internal Server Error
        }
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<BookingRequest>> getBookingsByCustomerId(@PathVariable Long customerId) {
        List<BookingRequest> bookings = bookingService.getBookingsByCustomerId(customerId);
        return bookings.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(bookings);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingRequest> getBookingById(@PathVariable Long id) {
        return bookingService.getBookingById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingRequest> updateBooking(@PathVariable Long id, @RequestBody BookingRequest booking) {
        try {
            BookingRequest updatedBooking = bookingService.updateBooking(id, booking);
            return ResponseEntity.ok(updatedBooking);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        try {
            bookingService.deleteBooking(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}