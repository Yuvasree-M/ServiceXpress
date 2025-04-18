package com.backend.controller;

import com.backend.model.Advisor;
import com.backend.model.CustomerServiceRequests;
import com.backend.service.AdvisorService; // Import your service
import com.backend.repository.CustomerServiceRequestsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/advisor")
public class AdvisorController {

    @Autowired
    private CustomerServiceRequestsRepository customerServiceRequestsRepository;

    @Autowired
    private AdvisorService advisorService; // Injecting the AdvisorService to handle logic for Advisor

    // Advisor can view their assigned service requests
    @GetMapping("/service-requests")
    public List<CustomerServiceRequests> getAssignedServiceRequests(@RequestParam Long advisorId) {
        return customerServiceRequestsRepository.findByAdvisorId(advisorId); // Fetching service requests by Advisor ID
    }

    // Fetch advisor by username
    @GetMapping("/findByUsername/{username}")
    public ResponseEntity<Advisor> getAdvisorByUsername(@PathVariable String username) {
        return advisorService.findAdvisorByUsername(username)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Fetch advisor by email
    @GetMapping("/findByEmail/{email}")
    public ResponseEntity<Advisor> getAdvisorByEmail(@PathVariable String email) {
        return advisorService.findAdvisorByEmail(email)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Fetch advisor by phone number
    @GetMapping("/findByPhone/{phoneNumber}")
    public ResponseEntity<Advisor> getAdvisorByPhoneNumber(@PathVariable String phoneNumber) {
        return advisorService.findAdvisorByPhoneNumber(phoneNumber)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
