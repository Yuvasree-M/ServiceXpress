package com.frontend.controller;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.frontend.model.AdminDashboardData;

@Controller
public class AdminController {

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        AdminDashboardData data = new AdminDashboardData(100, 50, 5000.0);
        model.addAttribute("dashboardData", data);
        return "admin-dashboard";
    }
}