package com.backend.service;

import com.backend.model.Advisor;
import com.backend.repository.AdvisorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdvisorService {

    @Autowired
    private AdvisorRepository advisorRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public List<Advisor> getAllAdvisors() {
        return advisorRepository.findAllActive(); // Fetch only active advisors
    }

    public Optional<Advisor> getAdvisorById(Long id) {
        return advisorRepository.findById(id)
                .filter(Advisor::getActive); // Ensure the advisor is active
    }

    public Advisor createAdvisor(Advisor advisor) {
        // Encrypt the password before saving
        if (advisor.getPassword() != null && !isPasswordEncrypted(advisor.getPassword())) {
            advisor.setPassword(passwordEncoder.encode(advisor.getPassword()));
        }
        advisor.setActive(true); // Ensure new advisors are active
        return advisorRepository.save(advisor);
    }

    public Optional<Advisor> updateAdvisor(Long id, Advisor updatedAdvisor) {
        return advisorRepository.findById(id).map(advisor -> {
            advisor.setCityName(updatedAdvisor.getCityName());
            advisor.setCenterName(updatedAdvisor.getCenterName());
            advisor.setUsername(updatedAdvisor.getUsername());
            advisor.setEmail(updatedAdvisor.getEmail());
            advisor.setPhoneNumber(updatedAdvisor.getPhoneNumber());
            // Only encrypt the password if a new one is provided and it's not already encrypted
            if (updatedAdvisor.getPassword() != null && !updatedAdvisor.getPassword().isEmpty() 
                    && !isPasswordEncrypted(updatedAdvisor.getPassword())) {
                advisor.setPassword(passwordEncoder.encode(updatedAdvisor.getPassword()));
            }
            advisor.setRole(updatedAdvisor.getRole());
            advisor.setActive(updatedAdvisor.getActive() != null ? updatedAdvisor.getActive() : advisor.getActive());
            return advisorRepository.save(advisor);
        });
    }

    public boolean softDeleteAdvisor(Long id) {
        return advisorRepository.findById(id).map(advisor -> {
            advisor.setActive(false); // Mark as inactive instead of deleting
            advisorRepository.save(advisor);
            return true;
        }).orElse(false);
    }

    // Helper method to check if a password is already encrypted (BCrypt passwords start with $2a$, $2b$, or $2y$)
    private boolean isPasswordEncrypted(String password) {
        return password != null && password.matches("^\\$2[aby]\\$.*");
    }
}