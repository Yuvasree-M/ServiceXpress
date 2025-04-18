package com.backend.service;

import com.backend.model.Advisor;
import com.backend.repository.AdvisorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AdvisorService {

    @Autowired
    private AdvisorRepository advisorRepository;

    // Fetch all advisors
    public List<Advisor> findAll() {
        return advisorRepository.findAll();
    }

    // Fetch advisor by ID
    public Optional<Advisor> findById(Long id) {
        return advisorRepository.findById(id);
    }

    // Create a new advisor
    public Advisor save(Advisor advisor) {
        return advisorRepository.save(advisor);
    }

    // Update an existing advisor
    public Advisor update(Long id, Advisor advisor) {
        // Optionally, you can add logic here to check if the advisor exists before updating
        return advisorRepository.save(advisor);
    }

    // Delete an advisor by ID
    public void delete(Long id) {
        advisorRepository.deleteById(id);
    }

    // Find advisor by username
    public Optional<Advisor> findAdvisorByUsername(String username) {
        return advisorRepository.findByUsername(username);
    }

    // Find advisor by email
    public Optional<Advisor> findAdvisorByEmail(String email) {
        return advisorRepository.findByEmail(email);
    }

    // Find advisor by phone number
    public Optional<Advisor> findAdvisorByPhoneNumber(String phoneNumber) {
        return advisorRepository.findByPhoneNumber(phoneNumber);
    }
}
