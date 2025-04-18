package com.frontend.controller;

import com.frontend.model.AuthResponse;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
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
    public String login(@RequestParam String identifier, @RequestParam String password, Model model, HttpSession session) {
        try {
            String url = backendApiUrl + "/auth/login";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            LoginRequest loginRequest = new LoginRequest(identifier, password);
            HttpEntity<LoginRequest> request = new HttpEntity<>(loginRequest, headers);
            AuthResponse response = restTemplate.postForObject(url, request, AuthResponse.class);

            if (response != null && response.getToken() != null && response.getRole() != null) {
                String token = response.getToken();
                session.setAttribute("token", token);
                String role = response.getRole();
                String redirectUrl = switch (role) {
                    case "ADMIN" -> "/dashboard/admin";
                    case "SERVICE_ADVISOR" -> "/dashboard/service-advisor";
                    case "CUSTOMER" -> "/dashboard/customer";
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

    @PostMapping("/auth/send-otp")
    @ResponseBody
    public ResponseEntity<?> sendOtp(@RequestBody OtpRequest request) {
        try {
            String url = backendApiUrl + "/auth/customer/send-otp";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<OtpRequest> httpRequest = new HttpEntity<>(request, headers);
            return restTemplate.postForEntity(url, httpRequest, String.class);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to send OTP: " + e.getMessage());
        }
    }

    @PostMapping("/auth/verify-otp")
    public String verifyOtp(@RequestBody VerifyOtpRequest request, HttpSession session, Model model) {
        try {
            String url = backendApiUrl + "/auth/customer/verify-otp";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<VerifyOtpRequest> httpRequest = new HttpEntity<>(request, headers);
            ResponseEntity<AuthResponse> response = restTemplate.postForEntity(url, httpRequest, AuthResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                AuthResponse authResponse = response.getBody();
                if (authResponse.getToken() != null && "CUSTOMER".equals(authResponse.getRole())) {
                    session.setAttribute("token", authResponse.getToken());
                    return "redirect:/dashboard/customer";
                }
            }
            model.addAttribute("error", "Invalid OTP or role");
            return "index";
        } catch (Exception e) {
            model.addAttribute("error", "Verification failed: " + e.getMessage());
            return "index";
        }
    }


    public static class OtpRequest {
        private String phoneNumber;
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    }

    public static class VerifyOtpRequest {
        private String phoneNumber;
        private String otp;
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        public String getOtp() { return otp; }
        public void setOtp(String otp) { this.otp = otp; }
    }
}