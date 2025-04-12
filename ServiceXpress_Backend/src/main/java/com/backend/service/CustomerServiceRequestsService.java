package com.backend.service;

import com.backend.model.CustomerServiceRequests;
import com.backend.repository.CustomerServiceRequestsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CustomerServiceRequestsService {

    private final CustomerServiceRequestsRepository repository;

    public CustomerServiceRequestsService(CustomerServiceRequestsRepository repository) {
        this.repository = repository;
    }

    public CustomerServiceRequests createRequest(CustomerServiceRequests request) {
        request.setRequestedDate(LocalDateTime.now());
        return repository.save(request);
    }

    public List<CustomerServiceRequests> getAllRequests() {
        return repository.findAll();
    }

    public CustomerServiceRequests updateRequest(Integer id, CustomerServiceRequests request) {
        CustomerServiceRequests existing = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Request not found"));
        existing.setServiceStatus(request.getServiceStatus());
        existing.setCompletedDate(request.getServiceStatus().equals("COMPLETED") ? LocalDateTime.now() : null);
        existing.setAdvisor(request.getAdvisor());
        return repository.save(existing);
    }
}