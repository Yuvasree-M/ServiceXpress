package com.backend.controller;

import com.backend.config.JwtUtil;
import com.backend.dto.AuthResponse;
import com.backend.dto.LoginRequest;
import com.backend.dto.OtpRequest;
import com.backend.dto.OtpVerificationRequest;
import com.backend.dto.UserDetailsResponse;
import com.backend.repository.AdvisorRepository;
import com.backend.repository.CustomerRepository;
import com.backend.service.OtpService;
import com.backend.service.TokenBlacklistService;
import com.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:8082")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;
    private final OtpService otpService;
    private final AdvisorRepository advisorRepository;
    private final CustomerRepository customerRepository;

    @Autowired
    public AuthController(UserService userService, JwtUtil jwtUtil, TokenBlacklistService tokenBlacklistService, OtpService otpService, AdvisorRepository advisorRepository, CustomerRepository customerRepository) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
        this.otpService = otpService;
        this.advisorRepository = advisorRepository;
        this.customerRepository = customerRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        logger.info("Attempting login for identifier: {}", request.getIdentifier());
        try {
            if (request.getIdentifier() == null || request.getIdentifier().isEmpty()) {
                logger.warn("Login failed: Identifier is null or empty");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Identifier is required");
            }
            if (request.getPassword() == null || request.getPassword().isEmpty()) {
                logger.warn("Login failed for identifier {}: Password is null or empty", request.getIdentifier());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Password is required");
            }

            String role = userService.authenticate(request.getIdentifier(), request.getPassword());
            if (role != null) {
                String token = jwtUtil.generateToken(request.getIdentifier(), role);
                logger.info("Login successful for identifier: {}. Role: {}", request.getIdentifier(), role);
                return ResponseEntity.ok(new AuthResponse(token, role));
            } else {
                logger.warn("Login failed for identifier: {}. Invalid credentials.", request.getIdentifier());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password");
            }
        } catch (Exception e) {
            logger.error("Login failed for identifier {}: {}", request.getIdentifier(), e.getMessage(), e);
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Login failed: " + e.getMessage());
        }
    }

    @PostMapping("/customer/send-otp")
    public ResponseEntity<Map<String, String>> sendOtp(@RequestBody OtpRequest request) {
        String phoneNumber = request.getPhoneNumber();
        logger.info("Received OTP request for phone number: {}", phoneNumber);

        Map<String, String> response = new HashMap<>();
        try {
            if (phoneNumber == null || phoneNumber.isEmpty()) {
                response.put("error", "Phone number is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            if (userService.customerRepository.findByPhoneNumber(phoneNumber).isPresent()) {
                String otp = otpService.generateOtp(phoneNumber);
                otpService.sendOtp(phoneNumber, otp);
                response.put("message", "OTP generated for " + phoneNumber + ". Check console for OTP.");
                response.put("otp", otp); // For testing purposes; remove in production
                return ResponseEntity.ok(response);
            }
            response.put("error", "Phone number not registered");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        } catch (Exception e) {
            logger.error("Failed to send OTP to {}: {}", phoneNumber, e.getMessage(), e);
            response.put("error", "Failed to generate OTP: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @PostMapping("/customer/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpVerificationRequest request) {
        String phoneNumber = request.getPhoneNumber();
        String otp = request.getOtp();
        logger.info("Verifying OTP for phone number: {}", phoneNumber);
        try {
            if (otpService.verifyOtp(phoneNumber, otp)) {
                String role = userService.authenticate(phoneNumber, null); // No password needed
                if (role != null) {
                    String token = jwtUtil.generateToken(phoneNumber, role);
                    return ResponseEntity.ok(new AuthResponse(token, role));
                }
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid user");
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid OTP");
        } catch (Exception e) {
            logger.error("Failed to verify OTP for {}: {}", phoneNumber, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to verify OTP: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        logger.info("Processing logout request");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                tokenBlacklistService.blacklistToken(token);
                logger.info("Token blacklisted successfully");
                return ResponseEntity.ok("Logged out successfully.");
            }
        }
        logger.warn("Invalid token provided for logout");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid token.");
    }

    @PostMapping("/me")
    public ResponseEntity<UserDetailsResponse> getUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            logger.warn("Unauthenticated access to /me endpoint");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
        }

        String identifier = authentication.getName(); // Phone number for customers
        String role = authentication.getAuthorities().stream()
            .map(auth -> auth.getAuthority().replace("ROLE_", ""))
            .findFirst()
            .orElse(null);

        logger.info("Fetching user details for identifier: {}, role: {}", identifier, role);

        if ("CUSTOMER".equals(role)) {
            return customerRepository.findByPhoneNumber(identifier)
                .map(customer -> {
                    UserDetailsResponse response = new UserDetailsResponse();
                    response.setCustomerId(customer.getId());
                    response.setName(customer.getUsername());
                    response.setEmail(customer.getEmail());
                    response.setPhoneNumber(customer.getPhoneNumber());
                    logger.info("Customer details fetched: customerId={}", customer.getId());
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    logger.warn("Customer not found for phone number: {}", identifier);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                });
        } else if ("SERVICE_ADVISOR".equals(role)) {
            return advisorRepository.findByUsername(identifier)
                .map(advisor -> {
                    UserDetailsResponse response = new UserDetailsResponse();
                    response.setAdvisorId(advisor.getId());
                    logger.info("Advisor details fetched: advisorId={}", advisor.getId());
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> {
                    logger.warn("Advisor not found for username: {}", identifier);
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
                });
        }

        logger.warn("Unsupported role for /me endpoint: {}", role);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
    }

}