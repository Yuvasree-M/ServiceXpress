package com.backend.controller;

import com.backend.dto.BookingResponseDTO;
import com.backend.model.Advisor;
import com.backend.model.BookingRequest;
import com.backend.model.ServiceCenter;
import com.backend.model.VehicleModel;
import com.backend.model.VehicleType;
import com.backend.repository.ServiceCenterRepository;
import com.backend.repository.VehicleModelRepository;
import com.backend.repository.VehicleTypeRepository;
import com.backend.service.BookingRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "http://localhost:8082")
@RequestMapping("/api/bookings")
public class BookingRequestController {

    private final BookingRequestService bookingService;

    @Autowired
    private VehicleTypeRepository vehicleTypeRepository;

    @Autowired
    private VehicleModelRepository vehicleModelRepository;

    @Autowired
    private ServiceCenterRepository serviceCenterRepository;

    public BookingRequestController(BookingRequestService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<BookingRequest> createBooking(@RequestBody BookingRequest booking) {
        try {
            // Validate IDs
            if (!vehicleTypeRepository.existsById(booking.getVehicleTypeId())) {
                System.err.println("Invalid vehicle type ID: " + booking.getVehicleTypeId());
                return ResponseEntity.badRequest().body(null); // 400 Bad Request
            }
            if (!vehicleModelRepository.existsById(booking.getVehicleModelId())) {
                System.err.println("Invalid vehicle model ID: " + booking.getVehicleModelId());
                return ResponseEntity.badRequest().body(null);
            }
            if (!serviceCenterRepository.existsById(booking.getServiceCenterId())) {
                System.err.println("Invalid service center ID: " + booking.getServiceCenterId());
                return ResponseEntity.badRequest().body(null);
            }

            BookingRequest savedBooking = bookingService.createBooking(booking);
            return ResponseEntity.status(201).body(savedBooking); // 201 Created
        } catch (Exception e) {
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
    public ResponseEntity<BookingResponseDTO> getBookingById(@PathVariable Long id) {
        Optional<BookingRequest> bookingOptional = bookingService.getBookingById(id);
        if (bookingOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        BookingRequest booking = bookingOptional.get();

        // Fetch and format additional details
        String vehicleTypeFormatted = booking.getVehicleTypeId() + " (Unknown)";
        String vehicleModelFormatted = booking.getVehicleModelId() + " (Unknown)";
        String serviceCenterFormatted = booking.getServiceCenterId() + " (Unknown)";

        try {
            Optional<VehicleType> vehicleType = vehicleTypeRepository.findById(booking.getVehicleTypeId());
            if (vehicleType.isPresent()) {
                vehicleTypeFormatted = booking.getVehicleTypeId() + " (" + vehicleType.get().getName() + ")";
            }

            Optional<VehicleModel> vehicleModel = vehicleModelRepository.findById(booking.getVehicleModelId());
            if (vehicleModel.isPresent()) {
                vehicleModelFormatted = booking.getVehicleModelId() + " (" + vehicleModel.get().getModelName() + ")";
            }

            Optional<ServiceCenter> serviceCenter = serviceCenterRepository.findById(booking.getServiceCenterId());
            if (serviceCenter.isPresent()) {
                serviceCenterFormatted = booking.getServiceCenterId() + " (" + serviceCenter.get().getCenterName() + ")";
            }
        } catch (Exception e) {
            System.err.println("Error fetching details for booking ID " + id + ": " + e.getMessage());
        }

        // Create response DTO
        BookingResponseDTO response = new BookingResponseDTO(booking, serviceCenterFormatted, vehicleTypeFormatted, vehicleModelFormatted);
        return ResponseEntity.ok(response);
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