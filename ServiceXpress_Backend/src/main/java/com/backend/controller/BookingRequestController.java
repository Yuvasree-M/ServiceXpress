package com.backend.controller;

import com.backend.model.BookingRequest;
import com.backend.service.BookingRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.backend.dto.BillOfMaterialDTO;

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

    @GetMapping("/bom/{id}")
    public ResponseEntity<Map<String, Object>> getBillOfMaterials(@PathVariable Long id) {
        try {
            Map<String, Object> receiptData = bookingService.getReceiptData(id);
            return ResponseEntity.ok(receiptData);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(null);
        }catch (RuntimeException e) {
            return ResponseEntity.status(404).body(null);
        } 
    }

    @PostMapping("/send-bill/{id}")
    public ResponseEntity<Void> sendBill(@PathVariable Long id) {
        try {
            Map<String, Object> receiptData = bookingService.getReceiptData(id);
            BookingRequest booking = bookingService.getBookingById(id)
                    .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));

            String customerEmail = booking.getCustomerEmail();
            String customerName = (String) receiptData.get("customerName");
            String customerNameInBom = customerName;
            String advisorName = (String) receiptData.get("advisorName");
            String serviceName = (String) receiptData.get("serviceName");
            Double total = (Double) receiptData.get("total");

            List<Map<String, Object>> materialsData = (List<Map<String, Object>>) receiptData.get("materials");
            List<BillOfMaterialDTO.Material> materials = new ArrayList<>();
            if (materialsData != null) {
                for (Map<String, Object> materialData : materialsData) {
                    BillOfMaterialDTO.Material material = new BillOfMaterialDTO.Material();
                    material.setMaterialName((String) materialData.get("materialName"));
                    material.setQuantity(((Number) materialData.get("quantity")).intValue());
                    material.setPrice(((Number) materialData.get("price")).doubleValue());
                    materials.add(material);
                }
            }

            bookingService.sendBillEmail(customerEmail, customerName, id, customerNameInBom, advisorName, serviceName, materials, total);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
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
}