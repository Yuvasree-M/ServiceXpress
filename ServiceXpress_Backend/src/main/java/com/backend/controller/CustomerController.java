package com.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.backend.model.CustomerServiceRequests;
import com.backend.repository.CustomerServiceRequestsRepository;

@RestController
@CrossOrigin(origins = "http://localhost:8082")
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private CustomerServiceRequestsRepository customerServiceRequestsRepository;

    // Customer can view their service requests
    @GetMapping("/service-requests")
    public List<CustomerServiceRequests> getServiceRequests(@RequestParam Long customerId) {
        return customerServiceRequestsRepository.findByCustomerId(customerId);
    }
}
