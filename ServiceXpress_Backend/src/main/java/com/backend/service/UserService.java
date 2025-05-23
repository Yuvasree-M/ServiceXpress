package com.backend.service;


import com.backend.repository.AdminRepository;
import com.backend.repository.CustomerRepository;
import com.backend.repository.AdvisorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final AdminRepository adminRepository;
    public final CustomerRepository customerRepository;
    private final AdvisorRepository advisorRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(AdminRepository adminRepository,
                       CustomerRepository customerRepository,
                       AdvisorRepository advisorRepository,
                       PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.customerRepository = customerRepository;
        this.advisorRepository = advisorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String authenticate(String identifier, String password) {
        logger.info("Authenticating identifier: {}", identifier);
        
       
        String role = adminRepository.findByUsername(identifier)
                .filter(user -> {
                    if (password == null) {
                        logger.warn("Password is null for Admin authentication attempt with identifier: {}", identifier);
                        return false;
                    }
                    boolean matches = passwordEncoder.matches(password, user.getPassword());
                    if (!matches) {
                        logger.warn("Password mismatch for Admin with identifier: {}", identifier);
                    }
                    return matches;
                })
                .map(user -> {
                    logger.info("Admin authentication success for: {}", user.getUsername());
                    return user.getRole().name();
                })
                .orElse(null);

        if (role != null) {
            return role;
        }

        
        role = customerRepository.findByPhoneNumber(identifier)
                .map(user -> {
                    logger.info("Customer found for phone: {}", user.getPhoneNumber());
                    return user.getRole().name();
                })
                .orElse(null);

        if (role != null) {
            return role;
        }

        
        role = advisorRepository.findByUsername(identifier)
                .filter(user -> {
                    if (password == null) {
                        logger.warn("Password is null for Service Advisor authentication attempt with identifier: {}", identifier);
                        return false;
                    }
                    boolean matches = passwordEncoder.matches(password, user.getPassword());
                    if (!matches) {
                        logger.warn("Password mismatch for Service Advisor with identifier: {}", identifier);
                    }
                    return matches;
                })
                .map(user -> {
                    logger.info("Service Advisor authentication success for: {}", user.getUsername());
                    return user.getRole().name();
                })
                .orElse(null);

        if (role == null) {
            logger.warn("Authentication failed for identifier: {}. No matching user found.", identifier);
        }
        return role;
    }

}