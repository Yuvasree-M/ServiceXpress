package com.backend.controller;

import com.backend.config.JwtUtil;
import com.backend.dto.AuthResponse;
import com.backend.dto.LoginRequest;
import com.backend.dto.OtpRequest;
import com.backend.dto.OtpVerificationRequest;
import com.backend.repository.AdvisorRepository;
import com.backend.service.OtpService;
import com.backend.service.TokenBlacklistService;
import com.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;
    private final OtpService otpService;
    private final AdvisorRepository advisorRepository;

    @Autowired
    public AuthController(UserService userService, JwtUtil jwtUtil, TokenBlacklistService tokenBlacklistService, OtpService otpService, AdvisorRepository advisorRepository) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
        this.otpService = otpService;
        this.advisorRepository = advisorRepository;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        System.out.println("Attempting login for identifier: " + request.getIdentifier());
        String role = userService.authenticate(request.getIdentifier(), request.getPassword());
        if (role != null) {
            String token = jwtUtil.generateToken(request.getIdentifier(), role);
            return ResponseEntity.ok(new AuthResponse(token, role));
        }
        return ResponseEntity.status(401).body("Invalid credentials");
    }

    @PostMapping("/customer/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody OtpRequest request) {
        String phoneNumber = request.getPhoneNumber();
        try {
            if (phoneNumber == null || phoneNumber.isEmpty()) {
                return ResponseEntity.status(400).body("Phone number is required");
            }
            if (userService.customerRepository.findByPhoneNumber(phoneNumber).isPresent()) {
                String otp = otpService.generateOtp(phoneNumber);
                otpService.sendOtp(phoneNumber, otp);
                return ResponseEntity.ok("OTP generated for " + phoneNumber + ". Check console for OTP.");
            }
            return ResponseEntity.status(404).body("Phone number not registered");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to generate OTP: " + e.getMessage());
        }
    }

    @PostMapping("/customer/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody OtpVerificationRequest request) {
        String phoneNumber = request.getPhoneNumber();
        String otp = request.getOtp();
        try {
            if (otpService.verifyOtp(phoneNumber, otp)) {
                String role = userService.authenticate(phoneNumber, null); // No password needed
                if (role != null) {
                    String token = jwtUtil.generateToken(phoneNumber, role);
                    return ResponseEntity.ok(new AuthResponse(token, role));
                }
                return ResponseEntity.status(401).body("Invalid user");
            }
            return ResponseEntity.status(401).body("Invalid OTP");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to verify OTP: " + e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtUtil.validateToken(token)) {
                tokenBlacklistService.blacklistToken(token);
                return ResponseEntity.ok("Logged out successfully.");
            }
        }
        return ResponseEntity.status(400).body("Invalid token.");
    }

    @PostMapping("/me")
    public ResponseEntity<UserDetailsResponse> getUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(401).body(null);
        }

        String username = authentication.getName();
        String role = authentication.getAuthorities().stream()
            .map(auth -> auth.getAuthority().replace("ROLE_", ""))
            .findFirst()
            .orElse(null);

        if ("SERVICE_ADVISOR".equals(role)) {
            return advisorRepository.findByUsername(username)
                .map(advisor -> {
                    UserDetailsResponse response = new UserDetailsResponse();
                    response.setAdvisorId(advisor.getId());
                    return ResponseEntity.ok(response);
                })
                .orElseGet(() -> ResponseEntity.status(404).body(null));
        }

        return ResponseEntity.status(400).body(null);
    }

    public static class UserDetailsResponse {
        private Long advisorId;

        public Long getAdvisorId() { return advisorId; }
        public void setAdvisorId(Long advisorId) { this.advisorId = advisorId; }
    }
}