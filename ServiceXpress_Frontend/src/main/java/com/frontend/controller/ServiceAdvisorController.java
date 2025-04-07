package com.frontend.controller;

import com.frontend.model.DashboardData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

@Controller
public class ServiceAdvisorController {
    
    @Value("${backend.api.url}")
    private String backendApiUrl;
    
    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/service-advisor/dashboard")
    public String serviceAdvisorDashboard(Model model, @RequestParam String identifier, @RequestParam String password) {
        try {
            String url = backendApiUrl + "/dashboard/service-advisor";
            HttpHeaders headers = new HttpHeaders();
            headers.setBasicAuth(identifier, password);
            HttpEntity<String> request = new HttpEntity<>(headers);
            DashboardData data = restTemplate.exchange(url, HttpMethod.GET, request, DashboardData.class).getBody();
            model.addAttribute("dashboardData", data);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to load dashboard data: " + e.getMessage());
        }
        return "service-advisor-dashboard";
    }
}