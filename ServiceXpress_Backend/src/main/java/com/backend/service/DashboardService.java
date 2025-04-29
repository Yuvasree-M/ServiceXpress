package com.backend.service;

import com.backend.model.DashboardData;
import com.backend.repository.AdvisorRepository;
import com.backend.repository.CustomerServiceRequestsRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final AdvisorRepository advisorRepository;
    private final CustomerServiceRequestsRepository requestsRepository;

    public DashboardService(AdvisorRepository advisorRepository,
                             CustomerServiceRequestsRepository requestsRepository) {
        this.advisorRepository = advisorRepository;
        this.requestsRepository = requestsRepository;
    }

    public DashboardData getAdminDashboardData() {
        DashboardData data = new DashboardData();
        data.setTotalUsers(100); // Placeholder - replace with actual count
        data.setTotalServices(50); // Placeholder - replace with actual count
        data.setTotalRevenue(5000.0); // Placeholder - replace with actual revenue
        return data;
    }

    public DashboardData getCustomerDashboardData() {
        DashboardData data = new DashboardData();
        data.setBookedServices(2); // Placeholder
        data.setLastServiceDate("2025-03-15"); // Placeholder
        data.setUpcomingAppointments(List.of("Oil Change - 2025-04-10")); // Placeholder
        return data;
    }

//    public DashboardData getServiceAdvisorDashboardData() {
//        DashboardData data = new DashboardData();
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
