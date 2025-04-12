package com.backend.controller;

import com.backend.model.CustomerServiceRequests;
import com.backend.service.CustomerServiceRequestsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/requests")
public class CustomerServiceRequestsController {

    private final CustomerServiceRequestsService service;

    public CustomerServiceRequestsController(CustomerServiceRequestsService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CustomerServiceRequests> createRequest(@RequestBody CustomerServiceRequests request) {
        return ResponseEntity.ok(service.createRequest(request));
    }

    @GetMapping
    public ResponseEntity<List<CustomerServiceRequests>> getAllRequests() {
        return ResponseEntity.ok(service.getAllRequests());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CustomerServiceRequests> updateRequest(@PathVariable Integer id, @RequestBody CustomerServiceRequests request) {
        return ResponseEntity.ok(service.updateRequest(id, request));
    }
}