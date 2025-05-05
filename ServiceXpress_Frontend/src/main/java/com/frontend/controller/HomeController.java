package com.frontend.controller;

import com.frontend.model.AuthResponse;
import com.frontend.model.LoginRequest;
import com.frontend.model.ProcessStep;
import com.frontend.model.Review;
import com.frontend.model.ServiceCard;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

@Controller
public class HomeController {

    @Value("${backend.api.url}")
    private String backendApiUrl;

    private final RestTemplate restTemplate;
    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    public HomeController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/")
    public String home(Model model) {
        logger.info("Loading home page");
        // Home Section
        model.addAttribute("welcomePart1", "Welcome to");
        model.addAttribute("welcomePart2", "ServiceXpress");
        model.addAttribute("welcomeMessage", "Your one-stop solution for vehicle maintenance and management.");

        // Services Section
        model.addAttribute("servicesPart1", "Our");
        model.addAttribute("servicesPart2", "Services");
        model.addAttribute("servicesSubtitle", "Comprehensive Vehicle Care Solutions");
        List<ServiceCard> services = Arrays.asList(
            new ServiceCard("fas fa-gas-pump", "Car Oil Change", "Precision oil changes."),
            new ServiceCard("fas fa-car-side", "Car Tire Service", "Tire rotation and balancing."),
            new ServiceCard("fas fa-tools", "Car Brake Repair", "Brake system inspection."),
            new ServiceCard("fas fa-link", "Bike Chain Maintenance", "Chain lubrication."),
            new ServiceCard("fas fa-bicycle", "Bike Tune-Up", "Complete bike tune-up."),
            new ServiceCard("fas fa-motorcycle", "Bike Brake Pad Replacement", "Brake pad replacement.")
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

    @GetMapping("/login")
    public String loginRedirect() {
        logger.info("Redirecting to home page with login parameter");
        return "redirect:/?login=true";
    }

    @PostMapping("/login")
    public String login(@RequestParam String identifier, @RequestParam String password, Model model, HttpSession session) {
        logger.info("Attempting login for identifier: {}", identifier);
        try {
            String url = backendApiUrl + "/auth/login";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            LoginRequest loginRequest = new LoginRequest(identifier, password);
            HttpEntity<LoginRequest> request = new HttpEntity<>(loginRequest, headers);

            ResponseEntity<AuthResponse> responseEntity = restTemplate.postForEntity(url, request, AuthResponse.class);
            logger.debug("Backend response status: {}", responseEntity.getStatusCode());
            logger.debug("Backend response body: {}", responseEntity.getBody());

            AuthResponse response = responseEntity.getBody();
            if (response == null) {
                logger.warn("Login failed: Response body is null");
                model.addAttribute("error", "Invalid credentials: No response from server");
                return "index";
            }

            logger.debug("Deserialized token: {}", response.getToken());
            logger.debug("Deserialized role: {}", response.getRole());

            if (response.getToken() != null && response.getRole() != null) {
                String token = response.getToken();
                session.setAttribute("token", token);
                session.setAttribute("username", identifier);
                logger.debug("Authentication successful, token stored in session");

                String role = response.getRole();
                String redirectUrl;
                switch (role) {
                    case "ADMIN":
                        redirectUrl = "/dashboard/admin";
                        logger.info("Redirecting admin to {}", redirectUrl);
                        break;
                    case "SERVICE_ADVISOR":
                        // Fetch advisorId using the token
                        Long advisorId = fetchAdvisorId(token);
                        if (advisorId == null) {
                            logger.error("Failed to fetch Advisor ID for SERVICE_ADVISOR role");
                            model.addAttribute("error", "Advisor ID not found. Please contact support.");
                            return "index";
                        }
                        redirectUrl = "/dashboard/advisor?advisorId=" + advisorId;
                        logger.info("Redirecting service advisor to {}", redirectUrl);
                        break;
                    case "CUSTOMER":
                        redirectUrl = "/dashboard/customer";
                        logger.info("Redirecting customer to {}", redirectUrl);
                        break;
                    default:
                        redirectUrl = "/";
                        logger.warn("Unknown role: {}, redirecting to home", role);
                }
                return "redirect:" + redirectUrl;
            }
            logger.warn("Login failed: Token or role is null in response");
            model.addAttribute("error", "Invalid credentials: Token or role missing");
            return "index";
        } catch (Exception e) {
            logger.error("Login failed for identifier {}: {}", identifier, e.getMessage(), e);
            model.addAttribute("error", "Login failed: " + e.getMessage());
            return "index";
        }
    }

    private Long fetchAdvisorId(String token) {
        try {
            String url = backendApiUrl + "/auth/me";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<UserDetailsResponse> response = restTemplate.postForEntity(url, request, UserDetailsResponse.class);
            logger.debug("Fetch advisorId response: {}", response.getBody());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().getAdvisorId();
            }
            logger.warn("Failed to fetch advisorId: Invalid response");
            return null;
        } catch (Exception e) {
            logger.error("Error fetching advisorId: {}", e.getMessage(), e);
            return null;
        }
    }

    @PostMapping("/auth/send-otp")
    @ResponseBody
    public ResponseEntity<?> sendOtp(@RequestBody OtpRequest request) {
        logger.info("Sending OTP to phone number: {}", request.getPhoneNumber());
        try {
            String url = backendApiUrl + "/auth/customer/send-otp";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<OtpRequest> httpRequest = new HttpEntity<>(request, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, httpRequest, String.class);
            logger.info("OTP sent successfully to {}", request.getPhoneNumber());
            return response;
        } catch (Exception e) {
            logger.error("Failed to send OTP to {}: {}", request.getPhoneNumber(), e.getMessage(), e);
            return ResponseEntity.status(500).body("Failed to send OTP: " + e.getMessage());
        }
    }

    @PostMapping("/auth/verify-otp")
    public String verifyOtp(@RequestBody VerifyOtpRequest request, HttpSession session, Model model) {
        logger.info("Verifying OTP for phone number: {}", request.getPhoneNumber());
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
                    session.setAttribute("username", request.getPhoneNumber());
                    logger.info("OTP verified successfully, redirecting customer to /dashboard/customer");
                    return "redirect:/dashboard/customer";
                }
            }
            logger.warn("OTP verification failed: Invalid OTP or role");
            model.addAttribute("error", "Invalid OTP or role");
            return "index";
        } catch (Exception e) {
            logger.error("OTP verification failed for phone number {}: {}", request.getPhoneNumber(), e.getMessage(), e);
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

    public static class UserDetailsResponse {
        private Long advisorId;

        public Long getAdvisorId() { return advisorId; }
        public void setAdvisorId(Long advisorId) { this.advisorId = advisorId; }
    }
}