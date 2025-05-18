
package com.backend.controller;

import com.backend.model.BookingRequest;
import com.backend.service.BookingRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.razorpay.RazorpayException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.backend.dto.BillOfMaterialDTO;
import com.backend.dto.PaymentRequest;
import com.backend.dto.PaymentResponse;
import com.backend.dto.PaymentVerificationRequest;
import com.backend.dto.ServiceHistory;

@RestController
@CrossOrigin(origins = "http://localhost:8082")
@RequestMapping("/api/bookings")
public class BookingRequestController {

    private final BookingRequestService bookingService;

    @Autowired
    public BookingRequestController(BookingRequestService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingRequest> createBooking(@RequestBody BookingRequest booking) {
        BookingRequest createdBooking = bookingService.createBooking(booking);
        return ResponseEntity.ok(createdBooking);
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<BookingRequest>> getBookingsByCustomerId(@PathVariable Long customerId) {
        List<BookingRequest> bookings = bookingService.getBookingsByCustomerId(customerId);
        return bookings.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(bookings);
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingRequest> getBookingById(@PathVariable Long id) {
        Optional<BookingRequest> booking = bookingService.getBookingById(id);
        return booking.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingRequest> updateBooking(@PathVariable Long id, @RequestBody BookingRequest booking) {
        BookingRequest updatedBooking = bookingService.updateBooking(id, booking);
        return ResponseEntity.ok(updatedBooking);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/bom/{bookingId}")
    public ResponseEntity<?> getBillOfMaterials(@PathVariable Long bookingId,
                                               @RequestParam(required = true) Long customerId) {
        if (customerId == null || customerId <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid customerId: " + customerId));
        }
        try {
            BillOfMaterialDTO bom = bookingService.getBillOfMaterials(bookingId, customerId);
            return ResponseEntity.ok(bom);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    @PostMapping("/send-bill/{id}")
    public ResponseEntity<String> sendBill(@PathVariable Long id) {
        bookingService.sendBill(id);
        return ResponseEntity.ok("Bill sent successfully");
    }
    
    @PostMapping("/start/{id}")
    public ResponseEntity<BookingRequest> startService(@PathVariable Long id) {
        try {
            BookingRequest updatedBooking = bookingService.startService(id);
            return ResponseEntity.ok(updatedBooking);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.status(400).body(null);
        }
    }

    @PostMapping("/complete")
    public ResponseEntity<BookingRequest> completeBooking(@RequestBody BillOfMaterialDTO bomDTO) {
        try {
            Long bookingId = bomDTO.getBookingId();
            if (bookingId == null) {
                return ResponseEntity.status(400).body(null);
            }
            BookingRequest updatedBooking = bookingService.markBookingCompleted(bookingId, bomDTO);
            return ResponseEntity.ok(updatedBooking);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
    
    @PostMapping("/create-payment/{bookingId}")
    public ResponseEntity<PaymentResponse> createPayment(@PathVariable Long bookingId, @RequestBody PaymentRequest request) throws RazorpayException {
        PaymentResponse response = bookingService.createPayment(bookingId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-payment")
    public ResponseEntity<String> verifyPayment(@RequestBody PaymentVerificationRequest request) throws RazorpayException {
        bookingService.verifyPayment(request);
        return ResponseEntity.ok("Payment verified successfully");
    }
    
    @GetMapping("/history/all")
    public ResponseEntity<?> getAllServiceHistory() {
        try {
            List<ServiceHistory> allServiceHistory = bookingService.getAllServiceHistory();
            return ResponseEntity.ok(allServiceHistory);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to fetch all service history: " + e.getMessage()));
        }
    }
}
