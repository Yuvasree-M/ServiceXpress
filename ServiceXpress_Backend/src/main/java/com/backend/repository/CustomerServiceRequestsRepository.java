package com.backend.repository;

import com.backend.model.CustomerServiceRequests;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CustomerServiceRequestsRepository extends JpaRepository<CustomerServiceRequests, Integer> {
    @Query("SELECT COUNT(r) FROM CustomerServiceRequests r WHERE r.serviceStatus = :status")
    long countByServiceStatus(@Param("status") String status);
}