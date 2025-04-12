package com.frontend.controller;

import com.frontend.model.Customer;
import com.frontend.model.ServiceStatus;
import com.frontend.model.ServiceHistory;
import com.frontend.model.CustomerService;
import com.frontend.model.DashboardData;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpSession;

import java.util.List;

@Controller
public class CustomerController {
    
    @Value("${backend.api.url}")
    private String backendApiUrl;

    private final RestTemplate restTemplate;
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.restTemplate = new RestTemplate();
        this.customerService = customerService;
    }

    @GetMapping("/dashboard/customer")
    public String showDashboard(Model model, HttpSession session) {
        // Check if customer is logged in
        Customer customer = customerService.getLoggedInCustomer();
        if (customer == null) {
            model.addAttribute("error", "No logged-in customer found. Please log in.");
            return "redirect:/login";
        }

        // Retrieve customer data
        List<ServiceStatus> ongoingServices = customerService.getOngoingServices(customer.getId());
        List<ServiceHistory> serviceHistory = customerService.getServiceHistory(customer.getId());

        // Add attributes to model
        model.addAttribute("customer", customer);
        model.addAttribute("cartCount", customerService.getCartCount(customer.getId()));
        model.addAttribute("ongoingServices", ongoingServices);
        model.addAttribute("serviceHistory", serviceHistory);

        try {
            // Get token from session
            String token = (String) session.getAttribute("token");
            if (token == null) {
                model.addAttribute("error", "No authentication token found. Please log in.");
                return "redirect:/login";
            }

            // Add token to model (for debugging purposes)
            model.addAttribute("token", token);

            // Make API call to backend
            String url = backendApiUrl + "/dashboard/customer";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<String> request = new HttpEntity<>(headers);
            DashboardData data = restTemplate.exchange(url, HttpMethod.GET, request, DashboardData.class).getBody();
            
            model.addAttribute("dashboardData", data);
        } catch (Exception e) {
            // Handle API call failure
            model.addAttribute("error", "Unable to fetch dashboard data: " + e.getMessage());
        }

        return "customer-dashboard";
    }
    
    @GetMapping("/customer/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}