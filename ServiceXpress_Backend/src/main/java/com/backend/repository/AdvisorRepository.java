package com.backend.repository;

import com.backend.model.Advisor;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdvisorRepository extends JpaRepository<Advisor, Long> {
    Optional<Advisor> findByUsername(String username); // Matches the 'advisor_username' column in the entity
    Optional<Advisor> findByEmail(String email);
    Optional<Advisor> findByPhoneNumber(String phoneNumber);
}
