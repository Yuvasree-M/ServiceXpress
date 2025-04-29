package com.backend.controller;

import com.backend.model.Customer;
import com.backend.service.CustomerService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@PreAuthorize("hasRole('ADMIN')")
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    @Autowired
    private CustomerService customerService;

    @GetMapping("/active")
    public ResponseEntity<Page<Customer>> getActiveCustomers(Pageable pageable) {
        Page<Customer> activeCustomers = customerService.getActiveCustomers(pageable);
        logger.info("Returning active customers: {}", activeCustomers.getContent());
        return ResponseEntity.ok(activeCustomers);
    }

    @GetMapping("/inactive")
    public ResponseEntity<Page<Customer>> getInactiveCustomers(Pageable pageable) {
        Page<Customer> inactiveCustomers = customerService.getInactiveCustomers(pageable);
        logger.info("Returning inactive customers: {}", inactiveCustomers.getContent());
        return ResponseEntity.ok(inactiveCustomers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        Customer customer = customerService.getCustomerById(id);
        logger.info("Returning customer with ID {}: {}", id, customer);
        return ResponseEntity.ok(customer);
    }

    @PostMapping
    public ResponseEntity<Customer> addCustomer(@Valid @RequestBody Customer customer) {
        Customer createdCustomer = customerService.addCustomer(customer);
        logger.info("Added customer: {}", createdCustomer);
        return ResponseEntity.ok(createdCustomer);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable Long id, @Valid @RequestBody Customer customer) {
        Customer updatedCustomer = customerService.updateCustomer(id, customer);
        logger.info("Updated customer with ID {}: {}", id, updatedCustomer);
        return ResponseEntity.ok(updatedCustomer);
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateCustomer(@PathVariable Long id) {
        customerService.deactivateCustomer(id);
        logger.info("Deactivated customer with ID: {}", id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/reactivate")
    public ResponseEntity<Void> reactivateCustomer(@PathVariable Long id) {
        customerService.reactivateCustomer(id);
        logger.info("Reactivated customer with ID: {}", id);
        return ResponseEntity.ok().build();
    }
}