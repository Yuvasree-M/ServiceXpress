package com.frontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.frontend.model.ServiceAdvisorDashboardData;

import java.util.Arrays;

@Controller
public class ServiceAdvisorController {

    @GetMapping("/service-advisor/dashboard")
    public String serviceAdvisorDashboard(Model model) {
        ServiceAdvisorDashboardData data = new ServiceAdvisorDashboardData(
            5, 10, Arrays.asList("Oil Change - Car #123", "Tire Rotation - Bike #456")
        );
        model.addAttribute("dashboardData", data);
        return "service-advisor-dashboard";
    }
}