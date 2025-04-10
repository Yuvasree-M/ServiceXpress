package com.frontend.controller;

import com.frontend.model.DashboardData;
import com.frontend.model.VehicleDue;
import com.frontend.model.Advisor;
import com.frontend.model.VehicleCompleted;
import com.frontend.model.VehicleUnderService;
import com.frontend.model.AdvisorRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
@Controller
public class AdminController {

    @Value("${backend.api.url}")
    private String backendApiUrl;

    private final RestTemplate restTemplate;

    public AdminController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/admin/dashboard")
    public String showDashboard(Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "No authentication token found. Please log in.");
            return "index";
        }

        // Load mock data for now
        addMockData(model);
        return "admin-dashboard";
    }

    private void addMockData(Model model) {
        DashboardData dashboardData = new DashboardData(
            5, 3, 2, 2, "Admin User",
            List.of(
                new VehicleDue("John Doe", "Honda Civic", "Sedan", "Oil Change", "Bangalore", 1L,
                    List.of(new Advisor(1L, "Alice Smith"), new Advisor(2L, "Bob Johnson")),
                    new Date(), new Date(), "Requested")
            ),
            List.of(
                new VehicleUnderService("Jane Smith", "Sedan", "Honda City", "Center A", "John Doe", "Engine Check", "In Progress")
            ),
            List.of(
                new VehicleCompleted(1L, "Sam White", "SUV", "Hyundai Creta", "Center A", "John Doe", "Oil Change, Brake Check",
                    new Date(), "Completed", false, false)
            ),
            List.of(
                new AdvisorRequest(1L, "Mark Advisor", "Wheel Alignment", "Jane Smith", "SUV", "Mahindra XUV500", "Center A", "Pending")
            )
        );

        model.addAttribute("dashboardData", dashboardData);
        model.addAttribute("profileName", dashboardData.getProfileName());
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}

