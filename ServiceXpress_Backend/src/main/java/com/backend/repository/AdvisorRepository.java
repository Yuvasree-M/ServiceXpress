package com.backend.repository;

import com.backend.model.Advisor;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AdvisorRepository extends JpaRepository<Advisor, Long> {
    Optional<Advisor> findByUsername(String username);
    Optional<Advisor> findByEmail(String email);
    Optional<Advisor> findByPhoneNumber(String phoneNumber);

    @Query("SELECT a FROM Advisor a WHERE a.active = true")
    List<Advisor> findAllActive();
}