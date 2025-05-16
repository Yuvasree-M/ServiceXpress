package com.frontend.controller;

import com.frontend.model.AuthResponse;
import com.frontend.model.LoginRequest;
import com.frontend.model.ProcessStep;
import com.frontend.model.Review;
import com.frontend.model.ServiceCard;
import com.frontend.model.SocialLink;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

//    @PostMapping("/login")
//    public String login(@RequestParam String phoneNumber, @RequestParam String otp, HttpSession session, Model model) {
//        logger.info("Verifying OTP for phone number: {}", phoneNumber);
//        try {
//            String url = backendApiUrl + "/auth/customer/verify-otp";
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_JSON);
//            HomeController.VerifyOtpRequest requestBody = new HomeController.VerifyOtpRequest();
//            HttpEntity<HomeController.VerifyOtpRequest> request = new HttpEntity<>(requestBody, headers);
//
//            ResponseEntity<AuthResponse> response = restTemplate.exchange(
//                url, HttpMethod.POST, request, AuthResponse.class
//            );
//
//            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
//                AuthResponse authResponse = response.getBody();
//                if (authResponse.getToken() != null && "CUSTOMER".equals(authResponse.getRole())) {
//                    session.setAttribute("token", authResponse.getToken());
//                    session.setAttribute("username", phoneNumber); // Store phone number as username
//                    logger.info("OTP verified successfully, redirecting customer to /dashboard/customer");
//                    return "redirect:/dashboard/customer";
//                }
//                logger.warn("OTP verification failed: Invalid role or token");
//                model.addAttribute("error", "Invalid OTP or role");
//                return "index";
//            } else {
//                logger.warn("OTP verification failed: Invalid OTP");
//                model.addAttribute("error", "Invalid OTP or login failed");
//                return "index";
//            }
//        } catch (HttpClientErrorException e) {
//            logger.error("HTTP error during OTP verification for {}: Status {}, Response: {}", phoneNumber, e.getStatusCode(), e.getResponseBodyAsString());
//            model.addAttribute("error", "Verification failed: " + e.getResponseBodyAsString());
//            return "index";
//        } catch (Exception e) {
//            logger.error("OTP verification failed for phone number {}: {}", phoneNumber, e.getMessage(), e);
//            model.addAttribute("error", "Verification failed: " + e.getMessage());
//            return "index";
//        }
//    }
    
    @PostMapping("/login")
    public String login(@RequestParam(required = false) String identifier,
                       @RequestParam(required = false) String password,
                       @RequestParam(required = false) String phoneNumber,
                       @RequestParam(required = false) String otp,
                       Model model, HttpSession session) {
        logger.info("Login attempt with identifier: {}, phoneNumber: {}", identifier, phoneNumber);

        try {
            // Handle customer login with phone number and OTP
            if (phoneNumber != null && !phoneNumber.trim().isEmpty() && otp != null && !otp.trim().isEmpty()) {
                logger.info("Verifying OTP for phone number: {}", phoneNumber);
                String url = backendApiUrl + "/auth/customer/verify-otp";
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                VerifyOtpRequest requestBody = new VerifyOtpRequest();
                HttpEntity<VerifyOtpRequest> request = new HttpEntity<>(requestBody, headers);

                ResponseEntity<AuthResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, AuthResponse.class
                );

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    AuthResponse authResponse = response.getBody();
                    if (authResponse.getToken() != null && "CUSTOMER".equals(authResponse.getRole())) {
                        session.setAttribute("token", authResponse.getToken());
                        session.setAttribute("username", phoneNumber);
                        session.setAttribute("role", authResponse.getRole());
                        logger.info("OTP verified successfully, redirecting customer to /dashboard/customer");
                        return "redirect:/dashboard/customer";
                    }
                    logger.warn("OTP verification failed: Invalid role or token");
                    model.addAttribute("error", "Invalid OTP or role");
                    return "index";
                } else {
                    logger.warn("OTP verification failed: Invalid OTP");
                    model.addAttribute("error", "Invalid OTP or login failed");
                    return "index";
                }
            }

            // Handle ADMIN and SERVICE_ADVISOR login with identifier and password
            if (identifier == null || identifier.trim().isEmpty()) {
                logger.warn("Login failed: Identifier is null or empty");
                model.addAttribute("error", "Identifier is required");
                return "index";
            }
            if (password == null || password.trim().isEmpty()) {
                logger.warn("Login failed for identifier {}: Password is null or empty", identifier);
                model.addAttribute("error", "Password is required");
                return "index";
            }

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
                logger.warn("Login failed for identifier {}: Response body is null", identifier);
                model.addAttribute("error", "Invalid credentials: No response from server");
                return "index";
            }

            logger.debug("Deserialized token: {}", response.getToken());
            logger.debug("Deserialized role: {}", response.getRole());

            if (response.getToken() != null && response.getRole() != null) {
                String token = response.getToken();
                session.setAttribute("token", token);
                session.setAttribute("username", identifier);
                session.setAttribute("role", response.getRole());
                logger.info("Authentication successful for identifier: {}, role: {}", identifier, response.getRole());

                String role = response.getRole();
                String redirectUrl;
                switch (role) {
                    case "ADMIN":
                        redirectUrl = "/dashboard/admin";
                        logger.info("Redirecting admin to {}", redirectUrl);
                        break;
                    case "SERVICE_ADVISOR":
                        Long advisorId = fetchAdvisorId1(token);
                        if (advisorId == null) {
                            logger.error("Failed to fetch Advisor ID for SERVICE_ADVISOR role for identifier: {}", identifier);
                            model.addAttribute("error", "Advisor ID not found. Please contact support.");
                            return "index";
                        }
                        redirectUrl = "/dashboard/advisor?advisorId=" + advisorId;
                        logger.info("Redirecting service advisor to {}", redirectUrl);
                        break;
                    default:
                        redirectUrl = "/";
                        logger.warn("Unknown role: {}, redirecting to home for identifier: {}", role, identifier);
                }
                return "redirect:" + redirectUrl;
            }
            logger.warn("Login failed for identifier: {}: Token or role is null in response", identifier);
            model.addAttribute("error", "Invalid credentials: Token or role missing");
            return "index";
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error during login for identifier/phoneNumber {}: Status {}, Response: {}", 
                identifier != null ? identifier : phoneNumber, e.getStatusCode(), e.getResponseBodyAsString());
            String errorMessage = e.getStatusCode() == HttpStatus.UNAUTHORIZED
                ? "Invalid credentials or OTP"
                : "Login failed: " + e.getResponseBodyAsString();
            model.addAttribute("error", errorMessage);
            return "index";
        } catch (Exception e) {
            logger.error("Login failed for identifier/phoneNumber {}: {}", 
                identifier != null ? identifier : phoneNumber, e.getMessage(), e);
            model.addAttribute("error", "Login failed: " + e.getMessage());
            return "index";
        }
    }

    // Placeholder for fetchAdvisorId method (unchanged)
    private Long fetchAdvisorId1(String token) {
        try {
            String url = backendApiUrl + "/auth/me";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<UserDetailsResponse> response = restTemplate.exchange(
                url, HttpMethod.POST, request, UserDetailsResponse.class
            );
            logger.debug("Fetch advisorId response status: {}, body: {}", response.getStatusCode(), response.getBody());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Long advisorId = response.getBody().getAdvisorId();
                if (advisorId != null) {
                    logger.info("Successfully fetched advisorId: {}", advisorId);
                    return advisorId;
                } else {
                    logger.warn("AdvisorId is null in response: {}", response.getBody());
                }
            } else {
                logger.warn("Failed to fetch advisorId: Status {}", response.getStatusCode());
            }
            return null;
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error fetching advisorId: Status {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            logger.error("Error fetching advisorId: {}", e.getMessage(), e);
            return null;
        }
    }
    

    @GetMapping("/dashboard/advisor")
    public String advisorDashboard(@RequestParam("advisorId") Long advisorId, HttpSession session, Model model) {
        logger.info("Accessing advisor dashboard for advisorId: {}", advisorId);
        
        // Verify the user is a SERVICE_ADVISOR
        String token = (String) session.getAttribute("token");
        String username = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("role");
        
        if (token == null || username == null || role == null) {
            logger.warn("Unauthorized access to advisor dashboard: No token, username, or role in session");
            model.addAttribute("error", "Please log in to access the advisor dashboard");
            return "redirect:/login";
        }

        if (!"SERVICE_ADVISOR".equals(role)) {
            logger.warn("Unauthorized access to advisor dashboard by user: {}. Role: {}", username, role);
            model.addAttribute("error", "You do not have permission to access the advisor dashboard");
            return "redirect:/";
        }

        model.addAttribute("advisorId", advisorId);
        model.addAttribute("username", username);
        model.addAttribute("dashboardTitle", "Service Advisor Dashboard");
        model.addAttribute("welcomeMessage", "Welcome, " + username + "!");
        
        return "advisor-dashboard";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        logger.info("Logging out user: {}", session.getAttribute("username"));
        
        String token = (String) session.getAttribute("token");
        if (token != null) {
            try {
                String url = backendApiUrl + "/auth/logout";
                HttpHeaders headers = new HttpHeaders();
                headers.setBearerAuth(token);
                HttpEntity<String> request = new HttpEntity<>(headers);
                ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
                logger.info("Backend logout response: {}", response.getBody());
            } catch (Exception e) {
                logger.error("Error during backend logout: {}", e.getMessage(), e);
            }
        }

        session.invalidate();
        logger.info("Session invalidated, redirecting to home page");
        return "redirect:/";
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
                Long advisorId = response.getBody().getAdvisorId();
                logger.info("Successfully fetched advisorId: {}", advisorId);
                return advisorId;
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
            ResponseEntity<Map> response = restTemplate.postForEntity(url, httpRequest, Map.class);
            logger.info("OTP sent successfully to {}", request.getPhoneNumber());

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, String> responseBody = response.getBody();
                if (responseBody.containsKey("otp")) {
                    logger.info("OTP for testing: {}", responseBody.get("otp"));
                }
                return ResponseEntity.ok(responseBody);
            } else {
                logger.warn("Failed to send OTP: Status {}", response.getStatusCode());
                return ResponseEntity.status(response.getStatusCode()).body(response.getBody());
            }
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error while sending OTP to {}: Status {}, Response: {}", request.getPhoneNumber(), e.getStatusCode(), e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("Failed to send OTP to {}: {}", request.getPhoneNumber(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send OTP: " + e.getMessage());
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
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error during OTP verification for {}: Status {}, Response: {}", request.getPhoneNumber(), e.getStatusCode(), e.getResponseBodyAsString());
            model.addAttribute("error", "Verification failed: " + e.getResponseBodyAsString());
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