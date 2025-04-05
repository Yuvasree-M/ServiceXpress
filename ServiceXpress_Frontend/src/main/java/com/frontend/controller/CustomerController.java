package com.frontend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.frontend.model.CustomerDashboardData;

import java.util.Arrays;

@Controller
public class CustomerController {

    @GetMapping("/customer/dashboard")
    public String customerDashboard(Model model) {
        CustomerDashboardData data = new CustomerDashboardData(
            2, "2025-03-15", Arrays.asList("Oil Change - 2025-04-10")
        );
        model.addAttribute("dashboardData", data);
        return "customer-dashboard";
    }
}