package com.frontend.controller;

import com.frontend.model.BookingResponseDTO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.http.HttpSession;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Controller
public class AdvisorController {

    @Value("${backend.api.url}")
    private String backendApiUrl;

    private final RestTemplate restTemplate;
    private static final Logger logger = LoggerFactory.getLogger(AdvisorController.class);

    public AdvisorController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/dashboard/advisor")
    public String showAdvisorDashboard(@RequestParam(required = false) Long advisorId, Model model, HttpSession session) {
        logger.info("Accessing advisor dashboard for advisorId: {}", advisorId);
        
        String token = (String) session.getAttribute("token");
        if (token == null) {
            logger.warn("No authentication token found in session. Redirecting to login.");
            model.addAttribute("error", "No authentication token found. Please log in.");
            return "redirect:/login";
        }

        if (advisorId == null) {
            logger.warn("Advisor ID is null. Redirecting to error page.");
            model.addAttribute("error", "Advisor ID is required.");
            return "error";
        }

        try {
            String url = backendApiUrl + "/dashboard/advisor?advisorId=" + advisorId;
            logger.debug("Calling backend API: {}", url);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<BookingResponseDTO[]> response = restTemplate.exchange(
                    url, HttpMethod.GET, request, BookingResponseDTO[].class);

            if (response.getStatusCode() != HttpStatus.OK) {
                logger.error("Backend API returned status: {}", response.getStatusCode());
                model.addAttribute("error", "Failed to load advisor dashboard: " + response.getStatusCode());
                return "error";
            }

            List<BookingResponseDTO> advisorBookings = response.getBody() != null ?
                    Arrays.asList(response.getBody()) : Collections.emptyList();
            logger.info("Fetched {} bookings for advisorId: {}", advisorBookings.size(), advisorId);

            model.addAttribute("advisorBookings", advisorBookings);
            model.addAttribute("advisorId", advisorId);
            model.addAttribute("token", token);
        } catch (Exception e) {
            logger.error("Failed to load advisor dashboard for advisorId {}: {}", advisorId, e.getMessage(), e);
            model.addAttribute("error", "Failed to load advisor dashboard: " + e.getMessage());
            return "error";
        }

        return "advisor-dashboard";
    }
}