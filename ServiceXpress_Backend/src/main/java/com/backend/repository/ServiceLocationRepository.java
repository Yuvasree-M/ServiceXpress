package com.backend.repository;

import com.backend.model.Advisor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ServiceLocationRepository extends JpaRepository<Advisor, Long> {
    Optional<Advisor> findByAdvisorUsername(String username);
    Optional<Advisor> findByAdvisorEmail(String email);
    Optional<Advisor> findByAdvisorPhoneNumber(String phoneNumber);
}