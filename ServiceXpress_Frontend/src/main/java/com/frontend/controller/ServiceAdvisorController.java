package com.frontend.controller;

import com.frontend.model.DashboardData;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

@Controller
public class ServiceAdvisorController {
    
    @Value("${backend.api.url}")
    private String backendApiUrl;
    
    private final RestTemplate restTemplate;

    public ServiceAdvisorController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/service-advisor/dashboard")
    public String serviceAdvisorDashboard(Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");
        System.out.println("Token: " + token);
        if (token == null) {
            System.out.println("No token found in session");
            model.addAttribute("error", "No authentication token found. Please log in.");
            return "index";
        }

        try {
            String url = backendApiUrl + "/dashboard/service-advisor";
            System.out.println("Calling URL: " + url);
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<DashboardData> response = restTemplate.exchange(url, HttpMethod.GET, request, DashboardData.class);
            DashboardData data = response.getBody();
            System.out.println("Response status: " + response.getStatusCode());
            System.out.println("Response data: " + data);
            model.addAttribute("dashboardData", data);
            model.addAttribute("token", token);
        } catch (Exception e) {
            System.err.println("Error fetching dashboard data: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("error", "Failed to load dashboard data: " + e.getMessage());
        }
        return "service-advisor-dashboard";
    }
}