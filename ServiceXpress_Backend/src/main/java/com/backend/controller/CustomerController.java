package com.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.backend.model.Customer;
import com.backend.model.CustomerServiceRequests;
import com.backend.repository.CustomerServiceRequestsRepository;
import com.backend.service.CustomerService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final  CustomerServiceRequestsRepository customerServiceRequestsRepository;

    @GetMapping("/service-requests")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<List<CustomerServiceRequests>> getServiceRequests(@RequestParam Long customerId) {
        return ResponseEntity.ok(customerServiceRequestsRepository.findByCustomerId(customerId));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Customer> create(@RequestBody Customer customer) {
        return ResponseEntity.ok(customerService.createCustomer(customer));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Customer>> getAll() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getById(@PathVariable Long id) {
        return customerService.getCustomerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Customer> update(@PathVariable Long id, @RequestBody Customer customer) {
        customer.setId(id); // Ensure correct ID is set before saving
        return ResponseEntity.ok(customerService.updateCustomer(customer));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.ok().build();
    }
}
