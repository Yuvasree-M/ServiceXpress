package com.backend.controller;

import com.backend.model.DashboardData;
import com.backend.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {
    
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/admin")
    public ResponseEntity<DashboardData> getAdminDashboard() {
        return ResponseEntity.ok(dashboardService.getAdminDashboardData());
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