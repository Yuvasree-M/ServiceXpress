package com.frontend.controller;

import com.frontend.model.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

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

    // Admin dashboard with mock data fallback
    @GetMapping("/admin/dashboard")
    public String showDashboard(Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "No authentication token found. Please log in.");
            return "index";
        }

        addMockDashboardData(model); // mock fallback
        return "admin-dashboard";
    }

    private void addMockDashboardData(Model model) {
        DashboardData dashboardData = new DashboardData(
                1, 1, 1, 1, "Admin User",
                List.of(new VehicleDue(
                        "John Doe", "Honda Civic", "Sedan", "Oil Change", "Bangalore", 1L,
                        List.of(new Advisor(1L, "Alice Smith"), new Advisor(2L, "Bob Johnson")),
                        new Date(), new Date(), "Requested"
                )),
                List.of(new VehicleUnderService(
                        "Jane Smith", "Sedan", "Honda City", "Center A", "John Doe", "Engine Check", "In Progress"
                )),
                List.of(new VehicleCompleted(
                        1L, "Sam White", "SUV", "Hyundai Creta", "Center A", "John Doe",
                        "Oil Change, Brake Check", new Date(), "Completed", false, false
                )),
                List.of(new AdvisorRequest(
                        1L, "Mark Advisor", "Wheel Alignment", "Jane Smith", "SUV", "Mahindra XUV500", "Center A", "Pending"
                ))
        );

        model.addAttribute("dashboardData", dashboardData);
        model.addAttribute("profileName", dashboardData.getProfileName());
    }

    @GetMapping("/inventory")
    public String getInventory(Model model) {
        List<InventoryItem> items = List.of(
            new InventoryItem("Oil", "Lubricants", 10, "Liters", "A1", "2024-04-10")
        );
        model.addAttribute("inventoryItems", items);
        model.addAttribute("currentPage", 1);
        model.addAttribute("totalPages", 1);
        model.addAttribute("keyword", "");
        model.addAttribute("categories", List.of("Lubricants"));
        model.addAttribute("selectedCategory", "Lubricants");
        model.addAttribute("profileName", "Admin");

        return "inventory";
    }


    @GetMapping("/admin/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
