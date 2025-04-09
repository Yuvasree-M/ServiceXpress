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
        // Force mock data for testing
        addMockData(model);
        return "admin-dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    private void addMockData(Model model) {
        DashboardData dashboardData = new DashboardData(
            5, 3, 7, 2, "Admin User",
            Arrays.asList(
                new VehicleDue("John Doe", "Honda Civic", "Sedan", "Oil Change", "Bangalore", 
                    1L, Arrays.asList(new Advisor(1L, "Alice Smith"), new Advisor(2L, "Bob Johnson")), 
                    new Date(2024 - 1900, 3, 5), new Date(2024 - 1900, 3, 10), "Requested"),
                new VehicleDue("Emma Watson", "Toyota Corolla", "Sedan", "Brake Repair", "Mumbai", 
                    2L, Arrays.asList(new Advisor(1L, "Alice Smith"), new Advisor(2L, "Bob Johnson")), 
                    new Date(2024 - 1900, 3, 6), new Date(2024 - 1900, 3, 11), "Assigned")
            ),
            Arrays.asList(
                new VehicleUnderService("KA01CD5678", "Jane Smith", "Center A", "In Progress"),
                new VehicleUnderService("MH02EF9012", "Robert Lee", "Center B", "Assigned")
            ),
            Arrays.asList(
                new VehicleCompleted("KA02XY1122", "Sam White", new Date(2024 - 1900, 3, 1), "Completed"),
                new VehicleCompleted("DL04AB7788", "Amit Patel", new Date(2024 - 1900, 3, 2), "Completed")
            ),
            Arrays.asList(
                new AdvisorRequest("REQ123", "Mark Advisor", new Date(2024 - 1900, 3, 5), "Pending"),
                new AdvisorRequest("REQ124", "Sarah Jones", new Date(2024 - 1900, 3, 6), "Approved")
            )
        );
        System.out.println("Mock Data: " + dashboardData); // Debug output
        model.addAttribute("dashboardData", dashboardData);
        model.addAttribute("profileName", dashboardData.getProfileName());
    }
}