package com.frontend.controller;

import com.frontend.model.CustomerDashboardData;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;

@Controller
public class CustomerController {

    @GetMapping("/customer/dashboard")
    public String customerDashboard(Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");

        if (token == null) {
            model.addAttribute("error", "No authentication token found. Please log in.");
            return "index";
        }

        CustomerDashboardData dashboardData = new CustomerDashboardData();
        dashboardData.setBookedServices(3);
        dashboardData.setLastServiceDate("2024-12-15");
        dashboardData.setUpcomingAppointments(Arrays.asList(
                "Oil Change - 2025-04-12",
                "Tire Rotation - 2025-04-20"
        ));

        model.addAttribute("dashboardData", dashboardData);
        model.addAttribute("token", token);

        return "customer-dashboard";
    }
    
    @GetMapping("/customer/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

}
