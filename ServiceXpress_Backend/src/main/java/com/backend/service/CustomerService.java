package com.backend.service;

import com.backend.exception.CustomerNotFoundException;
import com.backend.model.Customer;
import com.backend.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public Page<Customer> getActiveCustomers(Pageable pageable) {
        return customerRepository.findByActive(true, pageable);
    }

    public Page<Customer> getInactiveCustomers(Pageable pageable) {
        return customerRepository.findByActive(false, pageable);
    }

    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with ID: " + id));
    }

    public Customer addCustomer(Customer customer) {
        customer.setPassword(passwordEncoder.encode(customer.getPassword()));
        customer.setActive(true);
        return customerRepository.save(customer);
    }

    public Customer updateCustomer(Long id, Customer updatedCustomer) {
        Customer customer = getCustomerById(id);
        customer.setUsername(updatedCustomer.getUsername());
        customer.setEmail(updatedCustomer.getEmail());
        customer.setPhoneNumber(updatedCustomer.getPhoneNumber());
        if (updatedCustomer.getPassword() != null && !updatedCustomer.getPassword().isEmpty()) {
            customer.setPassword(passwordEncoder.encode(updatedCustomer.getPassword()));
        }
        return customerRepository.save(customer);
    }

    public void deactivateCustomer(Long id) {
        Customer customer = getCustomerById(id);
        customer.setActive(false);
        customerRepository.save(customer);
    }

    public void reactivateCustomer(Long id) {
        Customer customer = getCustomerById(id);
        customer.setActive(true);
        customerRepository.save(customer);
    }
}