package com.backend.service;

import com.backend.model.Customer;
import com.backend.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    public List<Customer> getActiveCustomers() {
        return customerRepository.findAll().stream()
                .filter(Customer::getActive)
                .collect(Collectors.toList());
    }

    public List<Customer> getInactiveCustomers() {
        return customerRepository.findAll().stream()
                .filter(customer -> !customer.getActive())
                .collect(Collectors.toList());
    }

    public Customer getCustomerById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + id));
    }

    public Customer addCustomer(Customer customer) {
        customer.setActive(true); // Ensure new customers are active
        return customerRepository.save(customer);
    }

    public Customer updateCustomer(Long id, Customer updatedCustomer) {
        Customer customer = getCustomerById(id);
        customer.setUsername(updatedCustomer.getUsername());
        customer.setEmail(updatedCustomer.getEmail());
        customer.setPhoneNumber(updatedCustomer.getPhoneNumber());
        if (updatedCustomer.getPassword() != null && !updatedCustomer.getPassword().isEmpty()) {
            customer.setPassword(updatedCustomer.getPassword());
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