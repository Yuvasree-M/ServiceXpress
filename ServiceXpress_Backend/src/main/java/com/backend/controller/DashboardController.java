package com.backend.controller;

import com.backend.dto.AdvisorDTO;
import com.backend.dto.BookingResponseDTO;
import com.backend.dto.DashboardDataDTO;
import com.backend.dto.VehicleDueDTO;
import com.backend.dto.VehicleUnderServiceDTO;
import com.backend.model.Advisor;
import com.backend.model.BookingAdvisorMapping;
import com.backend.model.DashboardData;
import com.backend.service.BookingRequestService;
import com.backend.service.DashboardService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    
    private final DashboardService dashboardService;
    @Autowired
    private BookingRequestService bookingRequestService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/admin")
    public ResponseEntity<DashboardDataDTO> getAdminDashboardData() {
        try {
            // Fetch pending bookings
            List<BookingResponseDTO> pendingBookings = bookingRequestService.getPendingBookings();

            // Fetch all advisors
            List<Advisor> advisors = bookingRequestService.getAllAdvisors();
            List<AdvisorDTO> advisorDTOs = advisors.stream().map(advisor -> {
                AdvisorDTO dto = new AdvisorDTO();
                dto.setId(advisor.getId());
                dto.setName(advisor.getUsername());
                return dto;
            }).collect(Collectors.toList());

            // Map pending bookings to VehicleDueDTO
            List<VehicleDueDTO> vehiclesDue = pendingBookings.stream().map(booking -> {
                VehicleDueDTO vehicleDue = new VehicleDueDTO();
                vehicleDue.setId(booking.getId());
                vehicleDue.setOwnerName(booking.getCustomerName());
                vehicleDue.setVehicleModel(booking.getVehicleModelId());
                vehicleDue.setVehicleType(booking.getVehicleTypeId());
                vehicleDue.setServiceNeeded(booking.getServices());
                vehicleDue.setLocation(booking.getAddress());
                vehicleDue.setServiceAdvisorId(null);
                vehicleDue.setAvailableAdvisors(advisorDTOs);
                vehicleDue.setRequestedDate(booking.getRequestedDate());
                vehicleDue.setDueDate(booking.getRequestedDate().plusDays(2));
                vehicleDue.setStatus(booking.getStatus());
                return vehicleDue;
            }).collect(Collectors.toList());

            // Fetch in-progress bookings
            List<BookingResponseDTO> inProgressBookings = bookingRequestService.getInProgressBookings();

            // Map in-progress bookings to VehicleUnderServiceDTO
            List<VehicleUnderServiceDTO> vehiclesUnderService = inProgressBookings.stream().map(booking -> {
                VehicleUnderServiceDTO dto = new VehicleUnderServiceDTO();
                dto.setId(booking.getId());
                dto.setOwnerName(booking.getCustomerName());
                dto.setVehicleType(booking.getVehicleTypeId());
                dto.setVehicleModel(booking.getVehicleModelId());
                dto.setServiceCenter(booking.getServiceCenterId());
                String advisorName = "Unknown";
                Optional<BookingAdvisorMapping> mapping = bookingRequestService.getBookingAdvisorMappingRepository().findByBookingId(booking.getId());
                if (mapping.isPresent()) {
                    Optional<Advisor> advisor = bookingRequestService.getAdvisorRepository().findById(mapping.get().getAdvisorId());
                    advisorName = advisor.map(Advisor::getUsername).orElse("Unknown");
                }
                dto.setServiceAdvisor(advisorName);
                dto.setServiceAllocated(booking.getServices());
                dto.setStatus(booking.getStatus());
                return dto;
            }).collect(Collectors.toList());

            // Create DashboardDataDTO
            DashboardDataDTO dashboardData = new DashboardDataDTO();
            dashboardData.setDueCount(vehiclesDue.size());
            dashboardData.setServicingCount(vehiclesUnderService.size());
            dashboardData.setCompletedCount(0);
            dashboardData.setAdvisorRequestsCount(0);
            dashboardData.setProfileName("Admin User");
            dashboardData.setVehiclesDue(vehiclesDue);
            dashboardData.setVehiclesUnderService(vehiclesUnderService);
            dashboardData.setVehiclesCompleted(new ArrayList<>());
            dashboardData.setAdvisorRequests(new ArrayList<>());

            return ResponseEntity.ok(dashboardData);
        } catch (Exception e) {
            System.err.println("Error fetching dashboard data: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/customer")
    public ResponseEntity<DashboardData> getCustomerDashboard() {
        return ResponseEntity.ok(dashboardService.getCustomerDashboardData());
    }

//    @GetMapping("/service-advisor")
//    public ResponseEntity<DashboardData> getServiceAdvisorDashboard() {
//        return ResponseEntity.ok(dashboardService.getServiceAdvisorDashboardData());
//    }
}