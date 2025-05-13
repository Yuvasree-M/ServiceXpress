package com.frontend.controller;

import com.frontend.model.VehicleAssigned;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.HttpClientErrorException;
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

    @GetMapping("/service-advisor/dashboard")
    public String showAdvisorDashboard(@RequestParam(required = false) Long advisorId, Model model, HttpSession session) {
        logger.info("Accessing advisor dashboard for advisorId: {}", advisorId);

        String token = (String) session.getAttribute("token");
        String username = (String) session.getAttribute("username");
        if (token == null) {
            logger.warn("No authentication token found in session. Redirecting to login.");
            model.addAttribute("error", "No authentication token found. Please log in.");
            return "redirect:/login";
        }

        if (advisorId == null) {
            logger.warn("Advisor ID is null. Setting error message.");
            model.addAttribute("error", "Advisor ID is required.");
            model.addAttribute("assignedVehicles", Collections.emptyList());
            model.addAttribute("advisorId", 0L);
            model.addAttribute("token", token);
            model.addAttribute("username", username != null ? username : "Advisor");
            return "advisor-dashboard";
        }

        List<VehicleAssigned> assignedVehicles = Collections.emptyList();
        try {
            String url = backendApiUrl + "/advisors/assigned-vehicles?advisorId=" + advisorId;
            logger.debug("Calling backend API: {}", url);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<VehicleAssigned[]> response = restTemplate.exchange(
                    url, HttpMethod.GET, request, VehicleAssigned[].class);

            if (response.getStatusCode() == HttpStatus.OK) {
                assignedVehicles = response.getBody() != null ? Arrays.asList(response.getBody()) : Collections.emptyList();
                logger.info("Fetched {} vehicles for advisorId: {}", assignedVehicles.size(), advisorId);
                if (assignedVehicles.isEmpty()) {
                    model.addAttribute("error", "No assigned vehicles found for this advisor.");
                }
            } else {
                logger.error("Backend API returned unexpected status: {}", response.getStatusCode());
                model.addAttribute("error", "Failed to load vehicles: HTTP " + response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error fetching vehicles for advisorId {}: Status {}, Response: {}",
                    advisorId, e.getStatusCode(), e.getResponseBodyAsString());
            String errorMessage;
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                errorMessage = "Unauthorized: Invalid or expired token. Please log in again.";
            } else if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                errorMessage = "Access denied: You do not have permission to view these vehicles.";
            } else if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                errorMessage = "No vehicles found for advisor ID: " + advisorId;
            } else {
                errorMessage = "Failed to load vehicles: " + e.getResponseBodyAsString();
            }
            model.addAttribute("error", errorMessage);
        } catch (Exception e) {
            logger.error("Failed to load advisor dashboard for advisorId {}: {}", advisorId, e.getMessage(), e);
            model.addAttribute("error", "Failed to load vehicles: " + e.getMessage());
        }

        model.addAttribute("assignedVehicles", assignedVehicles);
        model.addAttribute("advisorId", advisorId);
        model.addAttribute("token", token);
        model.addAttribute("username", username != null ? username : "Advisor");
        logger.debug("Assigned vehicles set in model: {}", assignedVehicles);
        return "advisor-dashboard";
    }

    @PostMapping("/service-advisor/start-service")
    public String startService(@RequestParam Long bookingId, @RequestParam Long advisorId, HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "No authentication token found. Please log in.");
            return "redirect:/login";
        }

        try {
            String url = backendApiUrl + "/bookings/start/" + bookingId;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    url, HttpMethod.POST, request, String.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                model.addAttribute("error", "Failed to start service: HTTP " + response.getStatusCode());
            }
        } catch (Exception e) {
            model.addAttribute("error", "Failed to start service: " + e.getMessage());
        }

        return "redirect:/service-advisor/dashboard?advisorId=" + advisorId;
    }
}