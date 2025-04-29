package com.backend.service;

import com.backend.enums.ServiceStatus;
import com.backend.model.*;
import com.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    @Autowired
    private VehicleTypeRepository vehicleTypeRepository;
    
    @Autowired
    private VehicleModelRepository vehicleModelRepository;
    
    @Autowired
    private ServicePackageRepository servicePackageRepository;
    
    @Autowired
    private CustomerRepository customerRepository;
    
    @Autowired
    private AdvisorRepository advisorRepository;
    
    @Autowired
    private CustomerServiceRequestsRepository customerServiceRequestsRepository;

    @Autowired
    private JavaMailSender mailSender;

    // Method for Admin to create or update service package
    public ServicePackage saveServicePackage(ServicePackage servicePackage) {
        return servicePackageRepository.save(servicePackage);
    }

    // Method for Admin to assign service request to an advisor
    public CustomerServiceRequests assignServiceRequestToAdvisor(Integer requestId, Long advisorId) {
        CustomerServiceRequests request = customerServiceRequestsRepository.findById(requestId).orElseThrow();
        Advisor advisor = advisorRepository.findById(advisorId).orElseThrow();
        request.setAdvisor(advisor);
        request.setServiceStatus(ServiceStatus.PENDING);
        return customerServiceRequestsRepository.save(request);
    }

    // Method to mark service request as completed
    public CustomerServiceRequests completeServiceRequest(Integer requestId) {
        CustomerServiceRequests request = customerServiceRequestsRepository.findById(requestId).orElseThrow();
        request.setServiceStatus(ServiceStatus.COMPLETED);
        return customerServiceRequestsRepository.save(request);
    }

    // Method to send bill of materials to Admin
    public void sendBillToAdmin(CustomerServiceRequests request) {
        // Send an email with bill details (mocked for simplicity)
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("admin@vehicle.com");
        message.setSubject("Bill of Materials for Service Request");
        message.setText("Bill of materials for service request " + request.getRequestId() + " is ready.");
        mailSender.send(message);
    }

    // Method for Admin to send payment URL to Customer
    public void sendPaymentUrlToCustomer(CustomerServiceRequests request) {
        // Send payment link to customer
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(request.getCustomer().getEmail());
        message.setSubject("Payment Link for Your Service Request");
        message.setText("Click the following link to complete your payment: http://payment.url/ " + request.getRequestId());
        mailSender.send(message);
    }
    
    public void deleteServicePackage(Integer id) {
        servicePackageRepository.deleteById(id);
    }

    public void sendBillToAdminById(Integer requestId) {
        CustomerServiceRequests request = customerServiceRequestsRepository.findById(requestId).orElseThrow();
        sendBillToAdmin(request);
    }

    public void sendPaymentUrlToCustomerById(Integer requestId) {
        CustomerServiceRequests request = customerServiceRequestsRepository.findById(requestId).orElseThrow();
        sendPaymentUrlToCustomer(request);
    }

}
