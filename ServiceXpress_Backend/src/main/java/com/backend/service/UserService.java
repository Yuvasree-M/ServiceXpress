package com.backend.service;

import com.backend.model.Admin;
import com.backend.model.Customer;
import com.backend.model.Advisor;
import com.backend.repository.AdminRepository;
import com.backend.repository.CustomerRepository;
import com.backend.repository.ServiceLocationRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final AdminRepository adminRepository;
    private final CustomerRepository customerRepository;
    private final ServiceLocationRepository serviceLocationRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(AdminRepository adminRepository,
                      CustomerRepository customerRepository,
                      ServiceLocationRepository serviceLocationRepository,
                      PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.customerRepository = customerRepository;
        this.serviceLocationRepository = serviceLocationRepository;
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
            .or(() -> customerRepository.findByUsername(identifier)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .map(user -> {
                    System.out.println("Customer authentication success for: " + user.getUsername());
                    return user.getRole().name();
                }))
            .or(() -> serviceLocationRepository.findByAdvisorUsername(identifier)
                .filter(user -> passwordEncoder.matches(password, user.getAdvisorPassword()))
                .map(user -> {
                    System.out.println("Service Advisor authentication success for: " + user.getAdvisorUsername());
                    return user.getRole().name();
                }))
            .orElse(null);
    }

    public Admin createAdmin(Admin admin) {
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        return adminRepository.save(admin);
    }

    public Customer createCustomer(Customer customer) {
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        return customerRepository.save(customer);
    }

    public Advisor createServiceLocation(Advisor location) {
        location.setAdvisorPassword(passwordEncoder.encode(location.getAdvisorPassword()));
        return serviceLocationRepository.save(location);
    }

    public List<Advisor> getAllServiceLocations() {
        return serviceLocationRepository.findAll();
    }
}