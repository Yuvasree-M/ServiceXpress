package com.backend.service;

import com.backend.model.DashboardData;
import com.backend.repository.CustomerServiceRequestsRepository;
import com.backend.repository.ServiceLocationRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    
    private final ServiceLocationRepository serviceLocationRepository;
    private final CustomerServiceRequestsRepository requestsRepository;

    public DashboardService(ServiceLocationRepository serviceLocationRepository,
                           CustomerServiceRequestsRepository requestsRepository) {
        this.serviceLocationRepository = serviceLocationRepository;
        this.requestsRepository = requestsRepository;
    }

    public DashboardData getAdminDashboardData() {
        DashboardData data = new DashboardData();
        data.setTotalUsers(100);
        data.setTotalServices(50);
        data.setTotalRevenue(5000.0);
        return data;
    }

    public DashboardData getCustomerDashboardData() {
        DashboardData data = new DashboardData();
        data.setBookedServices(2);
        data.setLastServiceDate("2025-03-15");
        data.setUpcomingAppointments(Arrays.asList("Oil Change - 2025-04-10"));
        return data;
    }

    public DashboardData getServiceAdvisorDashboardData() {
        DashboardData data = new DashboardData();
        long pending = requestsRepository.countByServiceStatus("PENDING");
        long completed = requestsRepository.countByServiceStatus("COMPLETED");
        data.setPendingServices((int) pending);
        data.setCompletedServices((int) completed);
        List<String> assignedTasks = serviceLocationRepository.findAll().stream()
            .map(advisor -> "Task at " + advisor.getCenterName() + " by " + advisor.getAdvisorUsername())
            .collect(Collectors.toList());
        data.setAssignedTasks(assignedTasks);
        return data;
    }
}