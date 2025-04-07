package com.backend.service;

import com.backend.repository.AdminRepository;
import com.backend.repository.CustomerRepository;
import com.backend.repository.ServiceAdvisorRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final AdminRepository adminRepository;
    private final CustomerRepository customerRepository;
    private final ServiceAdvisorRepository serviceAdvisorRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(AdminRepository adminRepository,
                      CustomerRepository customerRepository,
                      ServiceAdvisorRepository serviceAdvisorRepository,
                      PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.customerRepository = customerRepository;
        this.serviceAdvisorRepository = serviceAdvisorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String authenticate(String identifier, String password) {
        System.out.println("Authenticating identifier: " + identifier + ", password: " + password);
        return adminRepository.findByUsername(identifier)
            .filter(user -> passwordEncoder.matches(password, user.getPassword()))
            .map(user -> {
                System.out.println("Admin authentication success for: " + user.getUsername());
                return user.getRole().name();
            })
            .or(() -> customerRepository.findByUsername(identifier)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .map(user -> {
                    System.out.println("Customer authentication success for: " + user.getUsername());
                    return user.getRole().name();
                }))
            .or(() -> serviceAdvisorRepository.findByUsername(identifier)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .map(user -> {
                    System.out.println("Service Advisor authentication success for: " + user.getUsername());
                    return user.getRole().name();
                }))
            .orElse(null);
    }
}