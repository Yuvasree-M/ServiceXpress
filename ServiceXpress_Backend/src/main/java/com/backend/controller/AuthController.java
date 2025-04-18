package com.backend.controller;

import com.backend.config.JwtUtil;
import com.backend.dto.AuthResponse;
import com.backend.dto.LoginRequest;
import com.backend.dto.OtpRequest;
import com.backend.dto.OtpVerificationRequest;
import com.backend.service.OtpService;
import com.backend.service.TokenBlacklistService;
import com.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final TokenBlacklistService tokenBlacklistService;
    private final OtpService otpService;

    public AuthController(UserService userService, JwtUtil jwtUtil, TokenBlacklistService tokenBlacklistService, OtpService otpService) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
        this.otpService = otpService;
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
}