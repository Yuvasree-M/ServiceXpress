package com.backend.controller;

import com.backend.model.Customer;
import com.backend.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

    @Autowired
    private CustomerService customerService;

    @GetMapping("/active")
    public ResponseEntity<List<Customer>> getActiveCustomers() {
        List<Customer> activeCustomers = customerService.getActiveCustomers();
        logger.info("Returning active customers: {}", activeCustomers);
        return ResponseEntity.ok(activeCustomers);
    }

    @GetMapping("/inactive")
    public ResponseEntity<List<Customer>> getInactiveCustomers() {
        List<Customer> inactiveCustomers = customerService.getInactiveCustomers();
        logger.info("Returning inactive customers: {}", inactiveCustomers);
        return ResponseEntity.ok(inactiveCustomers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Customer> getCustomerById(@PathVariable Long id) {
        Customer customer = customerService.getCustomerById(id);
        logger.info("Returning customer with ID {}: {}", id, customer);
        return ResponseEntity.ok(customer);
    }

    @PostMapping
    public ResponseEntity<Customer> addCustomer(@RequestBody Customer customer) {
        Customer createdCustomer = customerService.addCustomer(customer);
        logger.info("Added customer: {}", createdCustomer);
        return ResponseEntity.ok(createdCustomer);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Customer> updateCustomer(@PathVariable Long id, @RequestBody Customer customer) {
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