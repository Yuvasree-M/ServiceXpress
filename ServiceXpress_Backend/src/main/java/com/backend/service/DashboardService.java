package com.backend.service;

import com.backend.dto.AdvisorDTO;
import com.backend.dto.BookingResponseDTO;
import com.backend.dto.DashboardDataDTO;
import com.backend.dto.VehicleDueDTO;
import com.backend.model.DashboardData;
import com.backend.repository.AdvisorRepository;
import com.backend.repository.BookingRequestRepository;
import com.backend.repository.CustomerServiceRequestsRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final AdvisorRepository advisorRepository;
    private final CustomerServiceRequestsRepository requestsRepository;
    private final BookingRequestRepository bookingRequestRepository;
    private final BookingRequestService bookingService;

    public DashboardService(AdvisorRepository advisorRepository,BookingRequestService bookingService,
                             CustomerServiceRequestsRepository requestsRepository,BookingRequestRepository bookingRequestRepository) {
        this.advisorRepository = advisorRepository;
        this.requestsRepository = requestsRepository;
        this.bookingRequestRepository = bookingRequestRepository;
        this.bookingService = bookingService;
    }

    public DashboardDataDTO getAdminDashboardData() {
        DashboardDataDTO data = new DashboardDataDTO();

        // Fetch pending bookings
        List<BookingResponseDTO> pendingBookings = bookingService.getPendingBookings();
        List<VehicleDueDTO> vehiclesDue = pendingBookings.stream().map(booking -> {
            VehicleDueDTO due = new VehicleDueDTO();
            due.setId(booking.getId());
            due.setOwnerName(booking.getCustomerName());
            due.setVehicleModel(booking.getVehicleModelId());
            due.setVehicleType(booking.getVehicleTypeId());
            due.setServiceNeeded(booking.getServices());
            due.setLocation(booking.getServiceCenterId());
            due.setRequestedDate(booking.getRequestedDate());
            due.setDueDate(booking.getRequestedDate()); // Adjust as needed
            due.setStatus(booking.getStatus());
            // Fetch available advisors
            List<AdvisorDTO> advisors = bookingService.getAllAdvisors().stream()
                    .map(advisor -> new AdvisorDTO(advisor.getId(), advisor.getUsername()))
                    .collect(Collectors.toList());
            due.setAvailableAdvisors(advisors);
            return due;
        }).collect(Collectors.toList());

        data.setDueCount(vehiclesDue.size());
        data.setServicingCount(bookingRequestRepository.findByStatus("IN_PROGRESS").size());
        data.setCompletedCount(bookingRequestRepository.findByStatus("COMPLETED").size());
        data.setAdvisorRequestsCount(0); // Implement if needed
        data.setProfileName("Admin User");
        data.setVehiclesDue(vehiclesDue);
        data.setVehiclesUnderService(List.of()); // Implement for IN_PROGRESS
        data.setVehiclesCompleted(List.of()); // Implement for COMPLETED
        data.setAdvisorRequests(List.of()); // Implement if needed

        return data;
    }
    
    public DashboardData getCustomerDashboardData() {
        DashboardData data = new DashboardData();
        data.setBookedServices(2); // Placeholder
        data.setLastServiceDate("2025-03-15"); // Placeholder
        data.setUpcomingAppointments(List.of("Oil Change - 2025-04-10")); // Placeholder
        return data;
    }

//    public DashboardDataDTO getServiceAdvisorDashboardData() {
//        DashboardDataDTO data = new DashboardDataDTO();
//        long pending = requestsRepository.countByServiceStatus("PENDING");
//        long completed = requestsRepository.countByServiceStatus("COMPLETED");
//        data.setPendingServices((int) pending);
//        data.setCompletedServices((int) completed);
//
//        List<String> assignedTasks = advisorRepository.findAll().stream()
//            .map(advisor -> "Task at " + advisor.getCenterName() + " by " + advisor.getAdvisorUsername())
//            .collect(Collectors.toList());
//        data.setAssignedTasks(assignedTasks);
//
//        return data;
//    }
}
