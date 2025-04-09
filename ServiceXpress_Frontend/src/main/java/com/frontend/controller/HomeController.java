package com.frontend.controller;

import com.frontend.model.DashboardData;
import com.frontend.model.LoginRequest;
import com.frontend.model.ProcessStep;
import com.frontend.model.Review;
import com.frontend.model.Service;
import com.frontend.model.SocialLink;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Controller
public class HomeController {
    
    @Value("${backend.api.url}")
    private String backendApiUrl;
    
    private final RestTemplate restTemplate;

    public HomeController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/")
    public String home(Model model) {
        // Home Section
        model.addAttribute("welcomePart1", "Welcome to");
        model.addAttribute("welcomePart2", "ServiceXpress");
        model.addAttribute("welcomeMessage", "Your one-stop solution for vehicle maintenance and management.");

        // Services Section
        model.addAttribute("servicesPart1", "Our");
        model.addAttribute("servicesPart2", "Services");
        model.addAttribute("servicesSubtitle", "Comprehensive Vehicle Care Solutions");
        List<Service> services = Arrays.asList(
            new Service("fas fa-gas-pump", "Car Oil Change", "Precision oil changes."),
            new Service("fas fa-car-side", "Car Tire Service", "Tire rotation and balancing."),
            new Service("fas fa-tools", "Car Brake Repair", "Brake system inspection."),
            new Service("fas fa-link", "Bike Chain Maintenance", "Chain lubrication."),
            new Service("fas fa-bicycle", "Bike Tune-Up", "Complete bike tune-up."),
            new Service("fas fa-motorcycle", "Bike Brake Pad Replacement", "Brake pad replacement.")
        );
        model.addAttribute("services", services);

        // About Section
        model.addAttribute("aboutPart1", "About");
        model.addAttribute("aboutPart2", "ServiceXpress");
        model.addAttribute("aboutSubtitle", "Revolutionizing Vehicle Maintenance");
        List<String> aboutImages = Arrays.asList(
            "SeviceCenter1.jpg", "expert.jpg", "Service1.jpg", "HappyCustomer.jpg"
        );
        model.addAttribute("aboutImages", aboutImages);

        List<ProcessStep> processSteps = Arrays.asList(
            new ProcessStep("fas fa-calendar-alt", "Book Online", "Schedule your service."),
            new ProcessStep("fas fa-car", "Vehicle Drop-off", "Drop off your vehicle."),
            new ProcessStep("fas fa-sync", "Real-time Updates", "Live status updates."),
            new ProcessStep("fas fa-check-circle", "Vehicle Pickup", "Collect your vehicle.")
        );
        model.addAttribute("processSteps", processSteps);

        // Reviews Section
        List<Review> reviews = Arrays.asList(
            new Review("John D.", "<i class='fas fa-star'></i><i class='fas fa-star'></i><i class='fas fa-star'></i><i class='fas fa-star'></i><i class='fas fa-star'></i>", "Fast and reliable service!"),
            new Review("Sarah M.", "<i class='fas fa-star'></i><i class='fas fa-star'></i><i class='fas fa-star'></i><i class='fas fa-star'></i><i class='far fa-star'></i>", "Great experience!")
        );
        model.addAttribute("reviewsPart1", "Customer");
        model.addAttribute("reviewsPart2", "Reviews");
        model.addAttribute("reviewsSubtitle", "What Our Customers Say");
        model.addAttribute("reviews", reviews);

        // Footer Section
        model.addAttribute("footerAboutTitle", "About");
        model.addAttribute("footerAboutText", "Your trusted partner since 2025.");
        model.addAttribute("contactPhone", "+1 234 567 8900");
        model.addAttribute("contactPhoneLink", "tel:+12345678900");
        model.addAttribute("contactEmail", "info@servicexpress.com");
        model.addAttribute("contactEmailLink", "mailto:info@servicexpress.com");
        model.addAttribute("contactAddress", "123 Service St, Auto City");
        model.addAttribute("footerCopyright", "© 2025 ServiceXpress. All rights reserved.");
        List<SocialLink> socialLinks = Arrays.asList(
            new SocialLink("https://facebook.com", "fab fa-facebook-f"),
            new SocialLink("https://twitter.com", "fab fa-twitter"),
            new SocialLink("https://instagram.com", "fab fa-instagram")
        );
        model.addAttribute("socialLinks", socialLinks);

        return "index";
    }

    @PostMapping("/login")
    public String login(String identifier, String password, Model model, HttpSession session) {
        try {
            String url = backendApiUrl + "/auth/login";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            LoginRequest loginRequest = new LoginRequest(identifier, password);
            HttpEntity<LoginRequest> request = new HttpEntity<>(loginRequest, headers);
            AuthResponse response = restTemplate.postForObject(url, request, AuthResponse.class);

            if (response != null && response.getToken() != null && response.getRole() != null) {
                String token = response.getToken();
                session.setAttribute("token", token); // Store token in session
                String role = response.getRole();
                
                String redirectUrl = switch (role) {
                    case "ADMIN" -> "/admin/dashboard";
                    case "SERVICE_ADVISOR" -> "/service-advisor/dashboard";
                    case "CUSTOMER" -> "/customer/dashboard";
                    default -> "/";
                };
                return "redirect:" + redirectUrl;
            }
            model.addAttribute("error", "Invalid credentials");
            return "index";
        } catch (Exception e) {
            model.addAttribute("error", "Login failed: " + e.getMessage());
            return "index";
        }
    }

    // Inner class for AuthResponse
    public static class AuthResponse {
        private String token;
        private String role;

        public AuthResponse() {}

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
}