package com.backend.service;

import com.backend.dto.ContactRequestDTO;
import com.backend.model.Customer;
import com.backend.model.SupportTicket;
import com.backend.repository.CustomerRepository;
import com.backend.repository.SupportTicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class SupportTicketService {

    @Autowired
    private SupportTicketRepository supportTicketRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private EmailService emailService;

    @Transactional
    public SupportTicket createSupportTicket(ContactRequestDTO request) {
        SupportTicket ticket = new SupportTicket();
        ticket.setName(request.getName());
        ticket.setEmail(request.getEmail());
        ticket.setSubject(request.getSubject());
        ticket.setMessage(request.getMessage());

        // Link to customer if customerId is provided
        if (request.getCustomerId() != null) {
            Optional<Customer> customerOpt = customerRepository.findById(request.getCustomerId());
            if (customerOpt.isPresent()) {
                ticket.setCustomer(customerOpt.get());
            } else {
                throw new IllegalArgumentException("Customer not found with ID: " + request.getCustomerId());
            }
        }

        // Save the ticket
        SupportTicket savedTicket = supportTicketRepository.save(ticket);

        // Send email notification (optional)
        try {
            emailService.sendSupportQueryNotification(
                request.getName(),
                request.getEmail(),
                request.getSubject(),
                request.getMessage()
            );
        } catch (Exception e) {
            // Log the error but don't fail the ticket creation
            System.err.println("Failed to send email notification: " + e.getMessage());
        }

        return savedTicket;
    }
}