
package com.backend.controller;

import com.backend.dto.AdvisorDTO;
import com.backend.dto.BookingResponseDTO;
import com.backend.dto.DashboardDataDTO;
import com.backend.dto.VehicleDueDTO;
import com.backend.dto.VehicleUnderServiceDTO;
import com.backend.dto.VehicleAssignedDTO;
import com.backend.dto.VehicleCompletedDTO;
import com.backend.model.Advisor;
import com.backend.model.BookingAdvisorMapping;
import com.backend.model.BookingRequest;
import com.backend.repository.AdvisorRepository;
import com.backend.repository.BillOfMaterialRepository;
import com.backend.repository.BookingAdvisorMappingRepository;
import com.backend.service.BookingRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "http://localhost:8082")
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final BookingRequestService bookingRequestService;
    private final BookingAdvisorMappingRepository bookingAdvisorMappingRepository;
    private final AdvisorRepository advisorRepository;
    private final BillOfMaterialRepository billOfMaterialRepository;
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

    public DashboardController(
        BookingRequestService bookingRequestService,
        BookingAdvisorMappingRepository bookingAdvisorMappingRepository,
        AdvisorRepository advisorRepository,
        BillOfMaterialRepository billOfMaterialRepository
    ) {
        this.bookingRequestService = bookingRequestService;
        this.bookingAdvisorMappingRepository = bookingAdvisorMappingRepository;
        this.advisorRepository = advisorRepository;
        this.billOfMaterialRepository = billOfMaterialRepository;
    }

    @GetMapping("/admin")
    public ResponseEntity<DashboardDataDTO> getAdminDashboardData() {
        logger.info("Fetching admin dashboard data");
        try {
            // Fetch pending bookings (Vehicles Due)
            List<BookingResponseDTO> pendingBookings = bookingRequestService.getPendingBookings();
            logger.debug("Fetched {} pending bookings", pendingBookings.size());

            List<Advisor> advisors = bookingRequestService.getAllAdvisors();
            List<AdvisorDTO> advisorDTOs = advisors.stream().map(advisor -> {
                AdvisorDTO dto = new AdvisorDTO();
                dto.setId(advisor.getId());
                dto.setName(advisor.getUsername());
                return dto;
            }).collect(Collectors.toList());

            List<VehicleDueDTO> vehiclesDue = pendingBookings.stream().map(booking -> {
                VehicleDueDTO vehicleDue = new VehicleDueDTO();
                vehicleDue.setId(booking.getId());
                vehicleDue.setOwnerName(booking.getCustomerName());
                vehicleDue.setVehicleModel(booking.getVehicleModel());
                vehicleDue.setVehicleType(booking.getVehicleType());
                vehicleDue.setServiceNeeded(booking.getServices());
                vehicleDue.setLocation(booking.getAddress());
                vehicleDue.setServiceAdvisorId(null);
                vehicleDue.setAvailableAdvisors(advisorDTOs);
                vehicleDue.setRequestedDate(booking.getRequestedDate());
                vehicleDue.setDueDate(booking.getRequestedDate() != null ? booking.getRequestedDate().plusDays(2) : null);
                vehicleDue.setStatus(booking.getStatus());
                return vehicleDue;
            }).collect(Collectors.toList());

            // Fetch in-progress bookings (Vehicles Under Service)
            List<BookingResponseDTO> inProgressBookings = bookingRequestService.getInProgressBookings();
            logger.debug("Fetched {} in-progress bookings", inProgressBookings.size());

            List<VehicleUnderServiceDTO> vehiclesUnderService = inProgressBookings.stream().map(booking -> {
                VehicleUnderServiceDTO dto = new VehicleUnderServiceDTO();
                dto.setId(booking.getId());
                dto.setOwnerName(booking.getCustomerName());
                dto.setVehicleType(booking.getVehicleType());
                dto.setVehicleModel(booking.getVehicleModel());
                dto.setServiceCenter(booking.getServiceCenter());
                String advisorName = "Unknown";
                Optional<BookingAdvisorMapping> mapping = bookingAdvisorMappingRepository.findByBookingId(booking.getId());
                if (mapping.isPresent()) {
                    Optional<Advisor> advisor = advisorRepository.findById(mapping.get().getAdvisorId());
                    advisorName = advisor.map(Advisor::getUsername).orElse("Unknown");
                }
                dto.setServiceAdvisor(advisorName);
                dto.setServiceAllocated(booking.getServices());
                dto.setStatus(booking.getStatus());
                return dto;
            }).collect(Collectors.toList());

            // Fetch assigned bookings (Vehicles Assigned)
            List<BookingResponseDTO> assignedBookings = bookingRequestService.getAssignedBookings();
            logger.debug("Fetched {} assigned bookings", assignedBookings.size());

            List<VehicleAssignedDTO> vehiclesAssigned = assignedBookings.stream().map(booking -> {
                VehicleAssignedDTO dto = new VehicleAssignedDTO();
                dto.setId(booking.getId());
                dto.setCustomerName(booking.getCustomerName());
                dto.setVehicleModel(booking.getVehicleModel());
                dto.setVehicleType(booking.getVehicleType());
                String advisorName = "Unknown";
                Optional<BookingAdvisorMapping> mapping = bookingRequestService.getBookingAdvisorMappingRepository().findByBookingId(booking.getId());
                if (mapping.isPresent()) {
                    Optional<Advisor> advisor = bookingRequestService.getAdvisorRepository().findById(mapping.get().getAdvisorId());
                    advisorName = advisor.map(Advisor::getUsername).orElse("Unknown");
                }
                dto.setAdvisorName(advisorName);
                dto.setAssignedDate(booking.getUpdatedAt());
                dto.setStatus(booking.getStatus());
                dto.setServicesNeeded(booking.getServices());
                return dto;
            }).collect(Collectors.toList());

            // Fetch completed and payment pending bookings (Vehicles Completed)
            List<BookingResponseDTO> completedBookings = bookingRequestService.getCompletedBookings();
            logger.debug("Fetched {} completed or payment pending bookings", completedBookings.size());

            List<VehicleCompletedDTO> vehiclesCompleted = completedBookings.stream().map(booking -> {
                VehicleCompletedDTO dto = new VehicleCompletedDTO();
                dto.setId(booking.getId());
                dto.setCustomerId(booking.getCustomerId() != null ? booking.getCustomerId() : 0L); // Prevent null
                dto.setOwnerName(booking.getCustomerName());
                dto.setVehicleType(booking.getVehicleType());
                dto.setVehicleModel(booking.getVehicleModel());
                dto.setServiceCenter(booking.getServiceCenter());
                String advisorName = "Unknown";
                Optional<BookingAdvisorMapping> mapping = bookingAdvisorMappingRepository.findByBookingId(booking.getId());
                if (mapping.isPresent()) {
                    Optional<Advisor> advisor = advisorRepository.findById(mapping.get().getAdvisorId());
                    advisorName = advisor.map(Advisor::getUsername).orElse("Unknown");
                }
                dto.setServiceAdvisor(advisorName);
                dto.setServiceDone(booking.getServices());
                dto.setCompletedDate(booking.getUpdatedAt());
                dto.setStatus(booking.getStatus());
                dto.setCustomerEmail(booking.getCustomerEmail());
                boolean hasBom = billOfMaterialRepository.findByBookingId(booking.getId()).isPresent();
                logger.debug("Booking ID: {}, hasBom: {}", booking.getId(), hasBom);
                dto.setHasBom(hasBom);
                return dto;
            }).collect(Collectors.toList());

            // Populate DashboardDataDTO
            DashboardDataDTO dashboardData = new DashboardDataDTO();
            dashboardData.setDueCount(vehiclesDue.size());
            dashboardData.setServicingCount(vehiclesUnderService.size());
            dashboardData.setCompletedCount(vehiclesCompleted.size());
            dashboardData.setAdvisorRequestsCount(0); // Not implemented
            dashboardData.setAssignedCount(vehiclesAssigned.size());
            dashboardData.setProfileName("Admin User");
            dashboardData.setVehiclesDue(vehiclesDue);
            dashboardData.setVehiclesUnderService(vehiclesUnderService);
            dashboardData.setVehiclesCompleted(vehiclesCompleted);
            dashboardData.setAdvisorRequests(new ArrayList<>());
            dashboardData.setVehiclesAssigned(vehiclesAssigned);

            logger.info("Admin dashboard data prepared successfully");
            return ResponseEntity.ok(dashboardData);
        } catch (Exception e) {
            logger.error("Error fetching admin dashboard data: {}", e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/advisor")
    public ResponseEntity<List<BookingResponseDTO>> getAdvisorBookings(@RequestParam Long advisorId) {
        logger.info("Fetching bookings for advisorId: {}", advisorId);
        try {
            if (advisorId == null) {
                logger.warn("Advisor ID is null");
                return ResponseEntity.badRequest().body(Collections.emptyList());
            }

            List<BookingResponseDTO> advisorBookings = bookingRequestService.getAssignedBookingsForAdvisor(advisorId);
            logger.info("Fetched {} bookings for advisorId: {}", advisorBookings.size(), advisorId);
            return ResponseEntity.ok(advisorBookings);
        } catch (Exception e) {
            logger.error("Error fetching advisor bookings for advisorId {}: {}", advisorId, e.getMessage(), e);
            return ResponseEntity.status(500).body(Collections.emptyList());
        }
    }

    @PostMapping("/start-service")
    public ResponseEntity<BookingRequest> startService(@RequestParam Long bookingId) {
        logger.info("Starting service for bookingId: {}", bookingId);
        try {
            if (bookingId == null) {
                logger.warn("Booking ID is null");
                return ResponseEntity.badRequest().build();
            }

            BookingRequest updatedBooking = bookingRequestService.startService(bookingId);
            logger.info("Service started successfully for bookingId: {}", bookingId);
            return ResponseEntity.ok(updatedBooking);
        } catch (Exception e) {
            logger.error("Error starting service for bookingId {}: {}", bookingId, e.getMessage(), e);
            return ResponseEntity.status(500).build();
        }
    }
}
