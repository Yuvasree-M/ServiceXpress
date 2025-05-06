package com.backend.service;

import com.backend.enums.ServiceStatus;
import com.backend.model.*;
import com.backend.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminService.class);
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\+?[1-9]\\d{9,14}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$");

    @Autowired
    private VehicleTypeRepository vehicleTypeRepository;
    
    @Autowired
    private VehicleModelRepository vehicleModelRepository;
    
    @Autowired
    private ServicePackageRepository servicePackageRepository;
    
    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private AdvisorRepository advisorRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;
    
    public Admin getAdminByUsername(String username) {
        logger.debug("Fetching admin with username: {}", username);
        try {
            Optional<Admin> admin = adminRepository.findByUsername(username);
            if (admin.isEmpty()) {
                logger.error("Admin not found for username: {}", username);
                throw new RuntimeException("Admin not found for username: " + username);
            }
            logger.info("Admin found: username={}, email={}, phoneNumber={}",
                    admin.get().getUsername(), admin.get().getEmail(), admin.get().getPhoneNumber());
            return admin.get();
        } catch (Exception e) {
            logger.error("Failed to fetch admin for username: {}. Exception: {}", username, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch admin: " + e.getMessage());
        }
    }

    public Admin updateAdminProfile(String username, String phoneNumber, String oldPassword, String newPassword) {
        logger.debug("Updating profile for username: {}", username);
        Admin admin = getAdminByUsername(username);

        if (newPassword != null && !newPassword.isEmpty()) {
            if (oldPassword == null || !passwordEncoder.matches(oldPassword, admin.getPassword())) {
                logger.error("Invalid old password for username: {}", username);
                throw new RuntimeException("Invalid old password");
            }
            if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
                logger.error("New password does not meet complexity requirements for username: {}", username);
                throw new RuntimeException("New password must be at least 8 characters with letters and numbers");
            }
            admin.setPassword(passwordEncoder.encode(newPassword));
            logger.info("Password updated for username: {}", username);
        }

        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            if (!PHONE_PATTERN.matcher(phoneNumber).matches()) {
                logger.error("Invalid phone number format for username: {}", username);
                throw new RuntimeException("Phone number must be 10-15 digits, may start with +");
            }
            admin.setPhoneNumber(phoneNumber);
            logger.info("Phone number updated for username: {}", username);
        }

        try {
            Admin updatedAdmin = adminRepository.save(admin);
            logger.debug("Admin entity after save: username={}, email={}, phoneNumber={}",
                    updatedAdmin.getUsername(), updatedAdmin.getEmail(), updatedAdmin.getPhoneNumber());
            logger.info("Profile updated successfully for username: {}", username);
            return updatedAdmin;
        } catch (Exception e) {
            logger.error("Failed to save updated admin profile for username: {}. SQL Exception: {}", username, e.getMessage(), e);
            throw new RuntimeException("Failed to save updated profile: " + e.getMessage());
        }
    }

    public ServicePackage saveServicePackage(ServicePackage servicePackage) {
        logger.debug("Saving service package: {}", servicePackage.getId());
        return servicePackageRepository.save(servicePackage);
    }
  
    
    public void deleteServicePackage(Integer id) {
        logger.debug("Deleting service package: {}", id);
        servicePackageRepository.deleteById(id);
    }

   
}