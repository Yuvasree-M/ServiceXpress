package com.backend.controller;

import com.backend.config.JwtUtil;
import com.backend.dto.AuthResponse;
import com.backend.dto.LoginRequest;
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

    public AuthController(UserService userService, JwtUtil jwtUtil,  TokenBlacklistService tokenBlacklistService) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.tokenBlacklistService = tokenBlacklistService;
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