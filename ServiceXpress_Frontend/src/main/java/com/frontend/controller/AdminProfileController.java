package com.frontend.controller;

import com.frontend.model.AdminProfileDTO;
import com.frontend.model.AdminUpdateDTO;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class AdminProfileController {

    @Value("${backend.api.url}")
    private String backendApiUrl;

    private final RestTemplate restTemplate;
    private static final Logger logger = LoggerFactory.getLogger(AdminProfileController.class);

    public AdminProfileController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/admin/profile")
    public String showProfile(Model model, HttpSession session, @RequestParam(value = "mode", required = false) String mode) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            logger.warn("No authentication token found in session");
            model.addAttribute("error", "Please log in to access your profile.");
            return "redirect:/login";
        }

        String username = (String) session.getAttribute("username");
        if (username == null) {
            logger.warn("No username found in session");
            model.addAttribute("error", "Session expired. Please log in again.");
            return "redirect:/login";
        }

        AdminProfileDTO profile = null;
        try {
            String url = backendApiUrl + "/admin/profile?username=" + username;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            HttpEntity<String> request = new HttpEntity<>(headers);

            logger.debug("Calling backend API: {}", url);
            ResponseEntity<AdminProfileDTO> response = restTemplate.exchange(url, HttpMethod.GET, request, AdminProfileDTO.class);
            profile = response.getBody();
            logger.debug("Backend response status: {}", response.getStatusCode());
            logger.debug("Backend response body: {}", profile);

            if (profile == null) {
                logger.error("No profile data received from backend for username: {}", username);
                model.addAttribute("error", "Unable to retrieve profile data.");
            }
        } catch (HttpClientErrorException e) {
            logger.error("Failed to load profile for username: {}. Status: {}, Response: {}", username, e.getStatusCode(), e.getResponseBodyAsString(), e);
            String errorMessage = e.getStatusCode() == HttpStatus.NOT_FOUND 
                    ? "Profile not found for username: " + username 
                    : "Failed to load profile: " + (e.getResponseBodyAsString() != null && !e.getResponseBodyAsString().isEmpty() 
                            ? e.getResponseBodyAsString() 
                            : "Server error (" + e.getStatusCode() + ")");
            model.addAttribute("error", errorMessage);
        } catch (Exception e) {
            logger.error("Unexpected error loading profile for username: {}", username, e);
            model.addAttribute("error", "An unexpected error occurred while loading the profile: " + e.getMessage());
        }

        AdminUpdateDTO adminUpdate = new AdminUpdateDTO();
        if (profile != null && profile.getPhoneNumber() != null) {
            adminUpdate.setPhoneNumber(profile.getPhoneNumber());
        }
        model.addAttribute("profile", profile);
        model.addAttribute("adminUpdate", adminUpdate);
        model.addAttribute("mode", mode);
        model.addAttribute("phonePattern", "^\\+?[1-9]\\d{9,14}$");
        model.addAttribute("passwordPattern", "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$");

        return "admin-profile";
    }

    @PostMapping("/admin/profile/update")
    public String updateAdminProfile(@ModelAttribute("adminUpdate") AdminUpdateDTO adminUpdate, 
                                    HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");
        String username = (String) session.getAttribute("username");
        if (token == null || username == null) {
            logger.warn("No authentication token or username found in session");
            model.addAttribute("error", "Please log in to update your profile.");
            return "redirect:/login";
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setBearerAuth(token);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("username", username);
            if (adminUpdate.getPhoneNumber() != null && !adminUpdate.getPhoneNumber().isEmpty()) {
                body.add("phoneNumber", adminUpdate.getPhoneNumber());
            }
            if (adminUpdate.getOldPassword() != null && !adminUpdate.getOldPassword().isEmpty()) {
                body.add("oldPassword", adminUpdate.getOldPassword());
            }
            if (adminUpdate.getNewPassword() != null && !adminUpdate.getNewPassword().isEmpty()) {
                body.add("newPassword", adminUpdate.getNewPassword());
            }

            logger.debug("Request body: {}", body);
            HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
            String url = backendApiUrl + "/admin/profile/update";
            logger.debug("Calling backend API for update: {}", url);
            ResponseEntity<AdminProfileDTO> response = restTemplate.postForEntity(url, request, AdminProfileDTO.class);

            logger.debug("Backend update response status: {}", response.getStatusCode());
            logger.debug("Backend update response body: {}", response.getBody());
            if (response.getStatusCode() == HttpStatus.OK) {
                if (response.getBody() == null) {
                    logger.warn("Null response body received from backend for username: {}", username);
                    model.addAttribute("error", "Profile updated, but response data is missing.");
                } else {
                    model.addAttribute("success", "Profile updated successfully.");
                }
            } else {
                logger.warn("Unexpected response status: {}", response.getStatusCode());
                model.addAttribute("error", "Failed to update profile: Unexpected response from server.");
            }
        } catch (HttpClientErrorException e) {
            logger.error("Failed to update profile for username: {}. Status: {}, Response: {}", username, e.getStatusCode(), e.getResponseBodyAsString(), e);
            String errorMessage = e.getResponseBodyAsString() != null && !e.getResponseBodyAsString().isEmpty() 
                    ? "Failed to update profile: " + e.getResponseBodyAsString() 
                    : "Failed to update profile: Server error (" + e.getStatusCode() + ")";
            model.addAttribute("error", errorMessage);
        } catch (Exception e) {
            logger.error("Unexpected error updating profile for username: {}", username, e);
            model.addAttribute("error", "Failed to update profile: " + e.getMessage());
        }

        return "redirect:/admin/profile";
    }
}