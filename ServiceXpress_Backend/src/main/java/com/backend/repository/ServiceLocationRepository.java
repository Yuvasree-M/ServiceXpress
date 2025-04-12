package com.backend.repository;

import com.backend.model.ServiceLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ServiceLocationRepository extends JpaRepository<ServiceLocation, Long> {
    Optional<ServiceLocation> findByAdvisorUsername(String username);
    Optional<ServiceLocation> findByAdvisorEmail(String email);
    Optional<ServiceLocation> findByAdvisorPhoneNumber(String phoneNumber);
}