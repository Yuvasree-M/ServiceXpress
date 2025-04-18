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

    public List<Advisor> getAllAdvisors() {
        return advisorRepository.findAll();
    }

    public Optional<Advisor> getAdvisorById(Long id) {
        return advisorRepository.findById(id);
    }

    public Advisor createAdvisor(Advisor advisor) {
        return advisorRepository.save(advisor);
    }

    public Advisor updateAdvisor(Advisor advisor) {
        return advisorRepository.save(advisor);
    }

    public void deleteAdvisor(Long id) {
        advisorRepository.deleteById(id);
    }
    
    public Optional<Advisor> findAdvisorByUsername(String username) {
        return advisorRepository.findByUsername(username);
    }

    public Optional<Advisor> findAdvisorByEmail(String email) {
        return advisorRepository.findByEmail(email);
    }

    public Optional<Advisor> findAdvisorByPhoneNumber(String phoneNumber) {
        return advisorRepository.findByPhoneNumber(phoneNumber);
    }
}
