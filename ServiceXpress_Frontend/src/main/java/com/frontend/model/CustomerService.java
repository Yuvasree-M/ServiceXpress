package com.frontend.model;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerService {

    @Value("${backend.api.url:http://localhost:8081/api}")
    private String backendApiUrl;

    private final RestTemplate restTemplate;
    private final HttpSession session;

    public CustomerService(RestTemplate restTemplate, HttpSession session) {
        this.restTemplate = restTemplate;
        this.session = session;
    }

    public Customer getLoggedInCustomer() {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            System.err.println("No authentication token found in session");
            return null;
        }

        try {
            String url = backendApiUrl + "/auth/me";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<UserDetailsResponse> response = restTemplate.exchange(
                url, HttpMethod.POST, request, UserDetailsResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                UserDetailsResponse userDetails = response.getBody();
                Customer customer = new Customer();
                customer.setId(userDetails.getCustomerId());
                customer.setName(userDetails.getName());
                customer.setEmail(userDetails.getEmail());
                customer.setMobile(userDetails.getPhoneNumber());
                customer.setInitials(userDetails.getName() != null ?
                    userDetails.getName().substring(0, Math.min(2, userDetails.getName().length())).toUpperCase() : "");
                return customer;
            } else {
                System.err.println("No customer data returned from " + url);
                return null;
            }
        } catch (HttpClientErrorException e) {
            System.err.println("HTTP error fetching customer data: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            return null;
        } catch (Exception e) {
            System.err.println("Error fetching customer data: " + e.getMessage());
            return null;
        }
    }

    public int getCartCount(Long customerId) {
        // Replace with actual API call if needed
        return 3;
    }

    public List<ServiceStatus> getOngoingServices(Long customerId) {
        // Replace with API call to /api/customer/dashboard
        try {
            String url = backendApiUrl + "/customer/dashboard?customerId=" + customerId;
            String token = (String) session.getAttribute("token");
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<CustomerDashboardDTO> response = restTemplate.exchange(
                url, HttpMethod.GET, request, CustomerDashboardDTO.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().getOngoingServices();
            }
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Error fetching ongoing services: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<ServiceHistory> getServiceHistory(Long customerId) {
        // Replace with API call to /api/customer/dashboard
        try {
            String url = backendApiUrl + "/customer/dashboard?customerId=" + customerId;
            String token = (String) session.getAttribute("token");
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<CustomerDashboardDTO> response = restTemplate.exchange(
                url, HttpMethod.GET, request, CustomerDashboardDTO.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().getServiceHistory();
            }
            return new ArrayList<>();
        } catch (Exception e) {
            System.err.println("Error fetching service history: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private static class UserDetailsResponse {
        private Long customerId;
        private String name;
        private String email;
        private String phoneNumber;

        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    }
}