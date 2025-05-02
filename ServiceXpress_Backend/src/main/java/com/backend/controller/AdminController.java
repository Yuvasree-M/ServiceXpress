package com.backend.controller;

import com.backend.model.Admin;
import com.backend.service.AdminService;
import com.backend.dto.AdminProfileDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    @Autowired
    private AdminService adminService;

    @Secured("ROLE_ADMIN")
    @GetMapping("/profile")
    public ResponseEntity<AdminProfileDTO> getAdminProfile(@RequestParam String username) {
        logger.debug("Fetching profile for username: {}", username);
        try {
            Admin admin = adminService.getAdminByUsername(username);
            AdminProfileDTO adminProfileDTO = new AdminProfileDTO();
            adminProfileDTO.setUsername(admin.getUsername());
            adminProfileDTO.setEmail(admin.getEmail());
            adminProfileDTO.setPhoneNumber(admin.getPhoneNumber());
            adminProfileDTO.setRole(admin.getRole().toString());
          
            logger.info("Profile fetched successfully for username: {}. Response: {}", username, adminProfileDTO);
            return ResponseEntity.ok(adminProfileDTO);
        } catch (RuntimeException e) {
            logger.error("Error fetching admin profile for username: {}. Message: {}", username, e.getMessage());
            AdminProfileDTO errorDTO = new AdminProfileDTO(null, null, null, null);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDTO);
        } catch (Exception e) {
            logger.error("Unexpected error fetching admin profile for username: {}. Exception: {}", username, e.getMessage(), e);
            AdminProfileDTO errorDTO = new AdminProfileDTO(null, null, null, null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDTO);
        }
    }

    @Secured("ROLE_ADMIN")
    @PostMapping("/profile/update")
    public ResponseEntity<AdminProfileDTO> updateAdminProfile(
            @RequestParam String username,
            @RequestParam(value = "phoneNumber", required = false) String phoneNumber,
            @RequestParam(value = "oldPassword", required = false) String oldPassword,
            @RequestParam(value = "newPassword", required = false) String newPassword) {
        logger.debug("Updating profile for username: {}. PhoneNumber: {}, OldPassword: {}, NewPassword: {}",
                username, phoneNumber, oldPassword != null ? "[REDACTED]" : "null", newPassword != null ? "[REDACTED]" : "null");
        try {
            Admin updatedAdmin = adminService.updateAdminProfile(username, phoneNumber, oldPassword, newPassword);
            AdminProfileDTO adminProfileDTO = new AdminProfileDTO();
            adminProfileDTO.setUsername(updatedAdmin.getUsername());
            adminProfileDTO.setEmail(updatedAdmin.getEmail());
            adminProfileDTO.setPhoneNumber(updatedAdmin.getPhoneNumber());
            adminProfileDTO.setRole(updatedAdmin.getRole().toString());
         
            logger.info("Profile updated successfully for username: {}. Response: {}", username, adminProfileDTO);
            return ResponseEntity.ok(adminProfileDTO);
        } catch (RuntimeException e) {
            logger.error("Error updating admin profile for username: {}. Message: {}", username, e.getMessage());
            AdminProfileDTO errorDTO = new AdminProfileDTO(null, null, null, null);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDTO);
        } catch (Exception e) {
            logger.error("Unexpected error updating admin profile for username: {}. Exception: {}", username, e.getMessage(), e);
            AdminProfileDTO errorDTO = new AdminProfileDTO(null, null, null, null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDTO);
        }
    }
}