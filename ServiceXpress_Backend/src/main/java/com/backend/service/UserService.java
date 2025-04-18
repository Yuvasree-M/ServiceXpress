package com.backend.service;

import com.backend.model.Admin;
import com.backend.model.Customer;
import com.backend.model.Advisor;
import com.backend.repository.AdminRepository;
import com.backend.repository.CustomerRepository;
import com.backend.repository.AdvisorRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
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
        System.out.println("Authenticating identifier: " + identifier);
        return adminRepository.findByUsername(identifier)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .map(user -> {
                    System.out.println("Admin authentication success for: " + user.getUsername());
                    return user.getRole().name();
                })
                .or(() -> customerRepository.findByPhoneNumber(identifier) // Use phone number for customers
                        .map(user -> {
                            System.out.println("Customer found for phone: " + user.getPhoneNumber());
                            return user.getRole().name(); // No password check for customers
                        }))
                .or(() -> advisorRepository.findByUsername(identifier)
                        .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                        .map(user -> {
                            System.out.println("Service Advisor authentication success for: " + user.getUsername());
                            return user.getRole().name();
                        }))
                .orElse(null);
    }

    public Admin createAdmin(Admin admin) {
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        return adminRepository.save(admin);
    }

    public Customer createCustomer(Customer customer) {
        // No password encoding for customers (optional, since OTP-based)
        return customerRepository.save(customer);
    }

    public Advisor createAdvisor(Advisor advisor) {
        advisor.setPassword(passwordEncoder.encode(advisor.getPassword()));
        return advisorRepository.save(advisor);
    }

    public List<Advisor> getAllAdvisors() {
        return advisorRepository.findAll();
    }
}