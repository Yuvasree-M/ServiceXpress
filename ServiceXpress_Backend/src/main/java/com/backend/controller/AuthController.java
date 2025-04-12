package com.backend.controller;

import com.backend.config.JwtUtil;
import com.backend.dto.AuthResponse;
import com.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    public static class LoginRequest {
        private String identifier;
        private String password;

        public String getIdentifier() { return identifier; }
        public void setIdentifier(String identifier) { this.identifier = identifier; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
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
}