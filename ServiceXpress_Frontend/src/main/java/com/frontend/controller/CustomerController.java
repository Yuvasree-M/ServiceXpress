package com.frontend.controller;

import com.frontend.model.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Controller
public class CustomerController {
	
	

    @Value("${backend.api.url:http://localhost:8081/api}")
    private String backendApiUrl;

    private final RestTemplate restTemplate;
    private final CustomerService customerService;

    public CustomerController(RestTemplate restTemplate, CustomerService customerService) {
        this.restTemplate = restTemplate;
        this.customerService = customerService;
    }

    @GetMapping("/dashboard/customer")
    public String showDashboard(Model model, HttpSession session) {
        Customer customer = customerService.getLoggedInCustomer();
        if (customer == null) {
            model.addAttribute("error", "No logged-in customer found. Please log in.");
            return "redirect:/login";
        }

        List<ServiceStatus> ongoingServices = new ArrayList<>();
        List<ServiceHistory> serviceHistory = new ArrayList<>();
        try {
            String url = backendApiUrl + "/customer/dashboard?customerId=" + customer.getId();
            String token = (String) session.getAttribute("token");
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<CustomerDashboardDTO> response = restTemplate.exchange(
                url, HttpMethod.GET, request, CustomerDashboardDTO.class
            );
            CustomerDashboardDTO dashboardData = response.getBody();
            if (dashboardData != null) {
                ongoingServices = dashboardData.getOngoingServices() != null ? dashboardData.getOngoingServices() : new ArrayList<>();
                serviceHistory = dashboardData.getServiceHistory() != null ? dashboardData.getServiceHistory() : new ArrayList<>();
            } else {
                System.err.println("No dashboard data returned from " + url);
                model.addAttribute("error", "No dashboard data available.");
            }
        } catch (HttpClientErrorException e) {
            System.err.println("HTTP error fetching dashboard data: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            model.addAttribute("error", "Unable to fetch dashboard data: " + e.getStatusCode() + " : " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Error fetching dashboard data: " + e.getMessage());
            model.addAttribute("error", "Unable to fetch dashboard data: " + e.getMessage());
        }

        model.addAttribute("customer", customer);
        model.addAttribute("cartCount", customerService.getCartCount(customer.getId()));
        model.addAttribute("ongoingServices", ongoingServices);
        model.addAttribute("serviceHistory", serviceHistory);

        return "customer-dashboard";
    }

    @GetMapping("/customer/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    @GetMapping("/customer/services")
    public String showServicesPage(Model model, HttpSession session) {
        Customer customer = customerService.getLoggedInCustomer();
        if (customer == null) {
            model.addAttribute("error", "No logged-in customer found. Please log in.");
            return "redirect:/login";
        }

        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "No authentication token found. Please log in.");
            return "redirect:/login";
        }

        model.addAttribute("username", customer.getName());
        model.addAttribute("customer", customer);

        try {
            String url = backendApiUrl + "/service-packages";
            HttpEntity<String> request = new HttpEntity<>(new HttpHeaders());
            ResponseEntity<List<ServicePackage>> response = restTemplate.exchange(
                url, HttpMethod.GET, request, new ParameterizedTypeReference<List<ServicePackage>>(){}
            );
            List<ServicePackage> servicePackages = response.getBody();
            if (servicePackages == null || servicePackages.isEmpty()) {
                System.err.println("No service packages returned from " + url);
                model.addAttribute("error", "No services available at the moment.");
            }

            List<Service> services = new ArrayList<>();
            if (servicePackages != null && !servicePackages.isEmpty()) {
                for (ServicePackage sp : servicePackages) {
                    String duration = "1-2 days";
                    List<String> tasks = Arrays.asList("Standard service tasks", "Inspection");
                    if (sp.getPackageName().contains("Wash")) {
                        tasks = Arrays.asList("Exterior cleaning", "Drying", "Inspection");
                    } else if (sp.getPackageName().contains("Deluxe")) {
                        tasks = Arrays.asList("Full detailing", "Interior cleaning", "Waxing");
                    } else if (sp.getPackageName().contains("Maintenance")) {
                        tasks = Arrays.asList("Undercarriage cleaning", "Exterior wash");
                    }

                    Service service = new Service(
                        sp.getId(),
                        sp.getPackageName(),
                        sp.getDescription(),
                        sp.getPrice(),
                        duration,
                        tasks,
                        sp.getVehicleType().getName()
                    );
                    services.add(service);
                }
            }

            model.addAttribute("services", services);
        } catch (HttpClientErrorException e) {
            System.err.println("HTTP error fetching services: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            model.addAttribute("error", "Unable to fetch services: " + e.getStatusCode() + " : " + e.getResponseBodyAsString());
            model.addAttribute("services", Collections.emptyList());
        } catch (Exception e) {
            System.err.println("Error fetching services: " + e.getMessage());
            model.addAttribute("error", "Unable to fetch services: " + e.getMessage());
            model.addAttribute("services", Collections.emptyList());
        }

        return "customer-services";
    }

    @GetMapping("/customer/bookings")
    public String showBookingPage(Model model, HttpSession session) {
        Customer customer = customerService.getLoggedInCustomer();
        if (customer == null) {
            model.addAttribute("error", "No logged-in customer found. Please log in.");
            return "redirect:/login";
        }

        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "No authentication token found. Please log in.");
            return "redirect:/login";
        }

        try {
            String vehicleTypeUrl = backendApiUrl + "/vehicle-types";
            HttpEntity<String> request = new HttpEntity<>(new HttpHeaders());
            ResponseEntity<List<VehicleType>> vehicleTypeResponse = restTemplate.exchange(
                vehicleTypeUrl, HttpMethod.GET, request, new ParameterizedTypeReference<List<VehicleType>>(){}
            );
            List<VehicleType> vehicleTypeDTOs = vehicleTypeResponse.getBody();
            if (vehicleTypeDTOs == null || vehicleTypeDTOs.isEmpty()) {
                System.err.println("No vehicle types returned from " + vehicleTypeUrl);
                model.addAttribute("error", "No vehicle types available. Please try again later.");
            }

            String servicePackageUrl = backendApiUrl + "/service-packages";
            ResponseEntity<List<ServicePackage>> servicePackageResponse = restTemplate.exchange(
                servicePackageUrl, HttpMethod.GET, request, new ParameterizedTypeReference<List<ServicePackage>>(){}
            );
            List<ServicePackage> servicePackages = servicePackageResponse.getBody();
            if (servicePackages == null || servicePackages.isEmpty()) {
                System.err.println("No service packages returned from " + servicePackageUrl);
                model.addAttribute("error", "No service packages available. Please try again later.");
            }

            model.addAttribute("vehicleTypes", vehicleTypeDTOs != null ? vehicleTypeDTOs : Collections.emptyList());
            model.addAttribute("servicePackages", servicePackages != null ? servicePackages : Collections.emptyList());
        } catch (HttpClientErrorException e) {
            System.err.println("HTTP error fetching data: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            model.addAttribute("error", "Unable to fetch data: " + e.getStatusCode() + " : " + e.getResponseBodyAsString());
            model.addAttribute("vehicleTypes", Collections.emptyList());
            model.addAttribute("servicePackages", Collections.emptyList());
        } catch (Exception e) {
            System.err.println("Error fetching data: " + e.getMessage());
            model.addAttribute("error", "Unable to fetch data: " + e.getMessage());
            model.addAttribute("vehicleTypes", Collections.emptyList());
            model.addAttribute("servicePackages", Collections.emptyList());
        }

        Booking booking = new Booking();
        booking.setCustomerName(customer.getName());
        booking.setCustomerEmail(customer.getEmail());
        booking.setCustomerPhone(customer.getMobile() != null ? customer.getMobile() : "");
        booking.setPickDropOption("NONE");
        booking.setPickupDropoffOption("");
        model.addAttribute("booking", booking);
        model.addAttribute("customer", customer);
        model.addAttribute("initials", customer.getName() != null ? customer.getName().substring(0, Math.min(2, customer.getName().length())).toUpperCase() : "");

        return "customer-booking";
    }

    @PostMapping("/submit-booking")
    public String submitBooking(@ModelAttribute Booking booking, Model model, HttpSession session) {
        Customer customer = customerService.getLoggedInCustomer();
        if (customer == null) {
            model.addAttribute("error", "No logged-in customer found. Please log in.");
            return "redirect:/login";
        }

        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "No authentication token found. Please log in.");
            return "redirect:/login";
        }

        try {
            String url = backendApiUrl + "/bookings";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Booking> request = new HttpEntity<>(booking, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, request, String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                model.addAttribute("success", "Booking submitted successfully!");
                return "redirect:/dashboard/customer";
            } else {
                System.err.println("Failed to submit booking: " + response.getStatusCode() + " - " + response.getBody());
                model.addAttribute("error", "Failed to submit booking: " + response.getStatusCode() + " : " + response.getBody());
            }
        } catch (HttpClientErrorException e) {
            System.err.println("HTTP error submitting booking: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            model.addAttribute("error", "Failed to submit booking: " + e.getStatusCode() + " : " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Error submitting booking: " + e.getMessage());
            model.addAttribute("error", "Error submitting booking: " + e.getMessage());
        }
        return "customer-booking";
    }

    @GetMapping("/profile-management")
    public String showCustomerProfile(
            @RequestParam(value = "mode", defaultValue = "view") String mode,
            Model model,
            HttpSession session) {
        Customer customer = customerService.getLoggedInCustomer();
        if (customer == null) {
            model.addAttribute("error", "No logged-in customer found. Please log in.");
            return "redirect:/login";
        }

        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "No authentication token found. Please log in.");
            return "redirect:/login";
        }

        try {
            String url = backendApiUrl + "/customer/profile?customerId=" + customer.getId();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<CustomerProfileDTO> response = restTemplate.exchange(
                url, HttpMethod.GET, request, CustomerProfileDTO.class
            );

            CustomerProfileDTO profile = response.getBody();
            if (profile == null || profile.getUsername() == null) {
                model.addAttribute("error", "Unable to fetch profile data.");
            } else {
                model.addAttribute("profile", profile);
            }

            if ("edit".equals(mode)) {
                CustomerUpdateDTO customerUpdate = new CustomerUpdateDTO();
                customerUpdate.setUsername(profile != null ? profile.getUsername() : "");
                customerUpdate.setEmail(profile != null ? profile.getEmail() : "");
                model.addAttribute("customerUpdate", customerUpdate);
            }

            model.addAttribute("mode", mode);
        } catch (HttpClientErrorException e) {
            System.err.println("HTTP error fetching customer profile: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            model.addAttribute("error", "Unable to fetch profile data: " + e.getStatusCode() + " : " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Error fetching customer profile: " + e.getMessage());
            model.addAttribute("error", "Unable to fetch profile data: " + e.getMessage());
        }

        model.addAttribute("customer", customer);
        return "customer-profile";
    }

    @PostMapping("/profile-management/update")
    public String updateCustomerProfile(
            @ModelAttribute("customerUpdate") CustomerUpdateDTO customerUpdate,
            Model model,
            HttpSession session) {
        Customer customer = customerService.getLoggedInCustomer();
        if (customer == null) {
            model.addAttribute("error", "No logged-in customer found. Please log in.");
            return "redirect:/login";
        }

        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "No authentication token found. Please log in.");
            return "redirect:/login";
        }

        try {
            // Validate inputs
            if (customerUpdate.getUsername() != null && !customerUpdate.getUsername().trim().isEmpty()) {
                if (customerUpdate.getUsername().length() < 3) {
                    model.addAttribute("error", "Username must be at least 3 characters long");
                    model.addAttribute("mode", "edit");
                    model.addAttribute("customer", customer);
                    model.addAttribute("customerUpdate", customerUpdate);
                    return "customer-profile";
                }
            }
            if (customerUpdate.getEmail() != null && !customerUpdate.getEmail().trim().isEmpty()) {
                if (!customerUpdate.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                    model.addAttribute("error", "Invalid email format (e.g., example@domain.com)");
                    model.addAttribute("mode", "edit");
                    model.addAttribute("customer", customer);
                    model.addAttribute("customerUpdate", customerUpdate);
                    return "customer-profile";
                }
            }

            // Log the validated data
            System.out.println("Sending update request for customerId=" + customer.getId() +
                               ", username=" + customerUpdate.getUsername() +
                               ", email=" + customerUpdate.getEmail());

            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(backendApiUrl + "/customer/profile/update")
                    .queryParam("customerId", customer.getId());
            if (customerUpdate.getUsername() != null && !customerUpdate.getUsername().trim().isEmpty()) {
                builder.queryParam("username", customerUpdate.getUsername());
            }
            if (customerUpdate.getEmail() != null && !customerUpdate.getEmail().trim().isEmpty()) {
                builder.queryParam("email", customerUpdate.getEmail());
            }

            String url = builder.toUriString();
            System.out.println("Request URL: " + url);

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                url, HttpMethod.POST, request, String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                String profileUrl = backendApiUrl + "/customer/profile?customerId=" + customer.getId();
                ResponseEntity<CustomerProfileDTO> profileResponse = restTemplate.exchange(
                    profileUrl, HttpMethod.GET, request, CustomerProfileDTO.class
                );
                CustomerProfileDTO updatedProfile = profileResponse.getBody();
                if (updatedProfile == null || updatedProfile.getUsername() == null) {
                    model.addAttribute("error", "Failed to fetch updated profile.");
                } else {
                    System.out.println("Updated profile: " + updatedProfile);
                    model.addAttribute("success", "Profile updated successfully!");
                    model.addAttribute("profile", updatedProfile);
                }
            } else {
                model.addAttribute("error", "Failed to update profile: " + response.getBody());
            }
        } catch (HttpClientErrorException e) {
            System.err.println("HTTP error updating customer profile: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            model.addAttribute("error", e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Error updating customer profile: " + e.getMessage());
            model.addAttribute("error", "Failed to update profile: " + e.getMessage());
        }

        model.addAttribute("mode", "view");
        model.addAttribute("customer", customer);
        return "customer-profile";
    }
    
    @GetMapping("/contact")
    public String showContactPage(Model model, HttpSession session) {
        Customer customer = (Customer) session.getAttribute("customer");
        model.addAttribute("customer", customer);
        return "contact";
    }

    @PostMapping("/contact")
    public String submitContactForm(
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String subject,
            @RequestParam String message,
            Model model,
            HttpSession session) {
        Customer customer = (Customer) session.getAttribute("customer");
        String token = (String) session.getAttribute("token");

        try {
            // Validate form data
            if (name == null || name.trim().length() < 3) {
                throw new IllegalArgumentException("Name must be at least 3 characters long");
            }
            if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                throw new IllegalArgumentException("Invalid email format");
            }
            if (subject == null || subject.trim().length() < 5) {
                throw new IllegalArgumentException("Subject must be at least 5 characters long");
            }
            if (message == null || message.trim().length() < 10) {
                throw new IllegalArgumentException("Message must be at least 10 characters long");
            }

            // Prepare API request
            String url = backendApiUrl + "/contact";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (token != null) {
                headers.setBearerAuth(token);
            }

            ContactRequestDTO contactRequest = new ContactRequestDTO();
            contactRequest.setName(name);
            contactRequest.setEmail(email);
            contactRequest.setSubject(subject);
            contactRequest.setMessage(message);
            if (customer != null) {
                contactRequest.setCustomerId(customer.getId());
            }

            HttpEntity<ContactRequestDTO> request = new HttpEntity<>(contactRequest, headers);
            String response = restTemplate.exchange(url, HttpMethod.POST, request, String.class).getBody();

            model.addAttribute("success", response);
        } catch (HttpClientErrorException e) {
            System.err.println("HTTP error submitting contact form: {}");
            model.addAttribute("error", "Failed to send message: " + e.getResponseBodyAsString());
        } catch (IllegalArgumentException e) {
            System.err.println("Validation error: {}");
            model.addAttribute("error", e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error submitting contact form: {}");
            model.addAttribute("error", "Failed to send message: " + e.getMessage());
        }

        model.addAttribute("customer", customer);
        return "contact";
    }
}