package com.backend.repository;

import com.backend.model.CustomerServiceRequests;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

//public interface CustomerServiceRequestsRepository extends JpaRepository<CustomerServiceRequests, Integer> {
//    @Query("SELECT COUNT(r) FROM CustomerServiceRequests r WHERE r.serviceStatus = :status")
//    long countByServiceStatus(@Param("status") String status);
//}


@Repository
public interface CustomerServiceRequestsRepository extends JpaRepository<CustomerServiceRequests, Integer> {
    
    List<CustomerServiceRequests> findByAdvisorId(Long advisorId);
    List<CustomerServiceRequests> findByCustomerId(Long customerId);
}
