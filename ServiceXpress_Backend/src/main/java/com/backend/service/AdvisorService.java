package com.backend.service;

import com.backend.model.Advisor;
import com.backend.model.Customer;
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
    private final BCryptPasswordEncoder passwordEncoder;
    
    @Autowired
    public AdvisorService(BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }
    public List<Advisor> getAllAdvisors() {
        return advisorRepository.findAllActive(); 
    }

    public Optional<Advisor> getAdvisorById(Long id) {
        return advisorRepository.findById(id)
                .filter(Advisor::getActive); 
    }

    public Advisor createAdvisor(Advisor advisor) {
        if (advisor.getPassword() != null && !isPasswordEncrypted(advisor.getPassword())) {
            advisor.setPassword(passwordEncoder.encode(advisor.getPassword()));
        }
        advisor.setActive(true); 
        return advisorRepository.save(advisor);
    }

    public Optional<Advisor> updateAdvisor(Long id, Advisor updatedAdvisor) {
        return advisorRepository.findById(id).map(advisor -> {
            advisor.setCityName(updatedAdvisor.getCityName());
            advisor.setCenterName(updatedAdvisor.getCenterName());
            advisor.setUsername(updatedAdvisor.getUsername());
            advisor.setEmail(updatedAdvisor.getEmail());
            advisor.setPhoneNumber(updatedAdvisor.getPhoneNumber());
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
            advisor.setActive(false);
            advisorRepository.save(advisor);
            return true;
        }).orElse(false);
    }

    public boolean ReactiveAdvisor(Long id) {
        return advisorRepository.findById(id).map(advisor -> {
            advisor.setActive(true);
            advisorRepository.save(advisor);
            return true;
        }).orElse(false);
    }

    private boolean isPasswordEncrypted(String password) {
        return password != null && password.matches("^\\$2[aby]\\$.*");
    }
}