package com.backend.repository;

import com.backend.model.ServiceAdvisor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ServiceAdvisorRepository extends JpaRepository<ServiceAdvisor, Long> {
    Optional<ServiceAdvisor> findByUsername(String username);
    Optional<ServiceAdvisor> findByEmail(String email);
    Optional<ServiceAdvisor> findByPhoneNumber(String phoneNumber);
}