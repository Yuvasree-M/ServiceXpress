package com.frontend.controller;

import com.frontend.model.DashboardData;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;



@Controller
public class CustomerController {
    
    @Value("${backend.api.url}")
    private String backendApiUrl;
    
    private final RestTemplate restTemplate;

    public CustomerController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/customer/dashboard")
    public String customerDashboard(Model model, HttpSession session) {
        try {
            String token = (String) session.getAttribute("token");
            if (token == null) {
                model.addAttribute("error", "No authentication token found. Please log in.");
                return "index";
            }

            String url = backendApiUrl + "/dashboard/customer";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<String> request = new HttpEntity<>(headers);
            DashboardData data = restTemplate.exchange(url, HttpMethod.GET, request, DashboardData.class).getBody();
            model.addAttribute("dashboardData", data);
            model.addAttribute("token", token);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load dashboard data: " + e.getMessage());
        }
        return "customer-dashboard";
    }
    
    
}