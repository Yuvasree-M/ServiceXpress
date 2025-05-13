package com.backend.controller;

import com.backend.model.Advisor;
import com.backend.service.AdvisorService;
import com.backend.dto.VehicleAssignedDTO;
import com.backend.model.BookingAdvisorMapping;
import com.backend.model.BookingRequest;
import com.backend.model.VehicleModel;
import com.backend.model.VehicleType;
import com.backend.repository.VehicleModelRepository;
import com.backend.repository.VehicleTypeRepository;
import com.backend.service.BookingRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/advisors")
@CrossOrigin(origins = "http://localhost:8082")
public class AdvisorController {
	
    private static final Logger logger = LoggerFactory.getLogger(AdvisorController.class);

    private final BookingRequestService bookingService;
    private final VehicleTypeRepository vehicleTypeRepository;
    private final VehicleModelRepository vehicleModelRepository;

    @Autowired
    public AdvisorController(BookingRequestService bookingService,
                             VehicleTypeRepository vehicleTypeRepository,
                             VehicleModelRepository vehicleModelRepository) {
        this.bookingService = bookingService;
        this.vehicleTypeRepository = vehicleTypeRepository;
        this.vehicleModelRepository = vehicleModelRepository;
    }
    
    @Autowired
    private AdvisorService advisorService;

    @GetMapping
    public List<Advisor> getAllAdvisors() {
        return advisorService.getAllAdvisors(); // Returns only active advisors
    }

    @GetMapping("/{id}")
    public ResponseEntity<Advisor> getAdvisorById(@PathVariable Long id) {
        Optional<Advisor> advisor = advisorService.getAdvisorById(id);
        return advisor.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Advisor createAdvisor(@RequestBody Advisor advisor) {
        return advisorService.createAdvisor(advisor);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Advisor> updateAdvisor(@PathVariable Long id, @RequestBody Advisor updatedAdvisor) {
        Optional<Advisor> updated = advisorService.updateAdvisor(id, updatedAdvisor);
        return updated.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdvisor(@PathVariable Long id) {
        return advisorService.softDeleteAdvisor(id) ? ResponseEntity.ok().<Void>build() : ResponseEntity.notFound().build();
    }
    
    @GetMapping("/assigned-vehicles")
    public ResponseEntity<List<VehicleAssignedDTO>> getAssignedVehicles(@RequestParam Long advisorId) {
        logger.info("Fetching assigned vehicles for advisorId: {}", advisorId);
        
        // Fetch mappings for the advisor
        List<BookingAdvisorMapping> mappings = bookingService.getBookingAdvisorMappingRepository().findByAdvisorId(advisorId);
        logger.debug("Found {} mappings for advisorId: {}", mappings.size(), advisorId);
        
        // Extract booking IDs
        List<Long> bookingIds = mappings.stream().map(BookingAdvisorMapping::getBookingId).collect(Collectors.toList());
        logger.debug("Extracted booking IDs: {}", bookingIds);
        
        // Fetch bookings with status ASSIGNED or IN_PROGRESS
        List<BookingRequest> bookings = bookingService.getBookingRequestRepository().findByIdInAndStatus(bookingIds, "ASSIGNED");
        bookings.addAll(bookingService.getBookingRequestRepository().findByIdInAndStatus(bookingIds, "IN_PROGRESS"));
        logger.info("Fetched {} bookings for advisorId: {}", bookings.size(), advisorId);

        // Map bookings to DTOs
        List<VehicleAssignedDTO> vehicleAssignedDTOs = bookings.stream().map(booking -> {
            VehicleAssignedDTO dto = new VehicleAssignedDTO();
            dto.setId(booking.getId());
            dto.setCustomerName(booking.getCustomerName());
            dto.setAssignedDate(booking.getUpdatedAt());
            dto.setStatus(booking.getStatus());
            dto.setServicesNeeded(booking.getServices());

            // Fetch Vehicle Type
            String vehicleType = "Unknown";
            Optional<VehicleType> vehicleTypeOpt = vehicleTypeRepository.findById(booking.getVehicleTypeId());
            if (vehicleTypeOpt.isPresent()) {
                vehicleType = vehicleTypeOpt.get().getName();
            } else {
                logger.warn("Vehicle type not found for vehicleTypeId: {}", booking.getVehicleTypeId());
            }
            dto.setVehicleType(vehicleType);

            // Fetch Vehicle Model and Registration
            String vehicleModel = "Unknown";
            Optional<VehicleModel> vehicleModelOpt = vehicleModelRepository.findById(booking.getVehicleModelId());
            if (vehicleModelOpt.isPresent()) {
                vehicleModel = vehicleModelOpt.get().getModelName() + " - " + booking.getVehicleRegistrationNumber();
            } else {
                logger.warn("Vehicle model not found for vehicleModelId: {}", booking.getVehicleModelId());
            }
            dto.setVehicleModel(vehicleModel);

            return dto;
        }).collect(Collectors.toList());

        logger.info("Returning {} vehicle assignments for advisorId: {}", vehicleAssignedDTOs.size(), advisorId);
        return ResponseEntity.ok(vehicleAssignedDTOs);
    }
}