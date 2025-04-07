package com.backend.service;

import com.backend.model.DashboardData;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
public class DashboardService {
    
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
        data.setPendingServices(5);
        data.setCompletedServices(10);
        data.setAssignedTasks(Arrays.asList("Oil Change - Car #123", "Tire Rotation - Bike #456"));
        return data;
    }
}