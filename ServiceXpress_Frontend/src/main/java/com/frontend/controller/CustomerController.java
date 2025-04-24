
package com.frontend.controller;

import com.frontend.model.Booking;
import com.frontend.model.Customer;
import com.frontend.model.CustomerService;
import com.frontend.model.DashboardData;
import com.frontend.model.ServiceStatus;
import com.frontend.model.ServiceHistory;
import com.frontend.model.Service;
import com.frontend.model.ServicePackage;
import com.frontend.model.VehicleType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

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

        List<ServiceStatus> ongoingServices = customerService.getOngoingServices(customer.getId());
        List<ServiceHistory> serviceHistory = customerService.getServiceHistory(customer.getId());

        model.addAttribute("customer", customer);
        model.addAttribute("cartCount", customerService.getCartCount(customer.getId()));
        model.addAttribute("ongoingServices", ongoingServices);
        model.addAttribute("serviceHistory", serviceHistory);

        try {
            String token = (String) session.getAttribute("token");
            if (token == null) {
                model.addAttribute("error", "No authentication token found. Please log in.");
                return "redirect:/login";
            }

            model.addAttribute("token", token);

            String url = backendApiUrl + "/dashboard/customer";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<DashboardData> response = restTemplate.exchange(url, HttpMethod.GET, request, DashboardData.class);
            model.addAttribute("dashboardData", response.getBody());
        } catch (Exception e) {
            model.addAttribute("error", "Unable to fetch dashboard data: " + e.getMessage());
        }

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
            HttpEntity<String> request = new HttpEntity<>(new HttpHeaders()); // No token needed
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
            // Fetch vehicle types
            String vehicleTypeUrl = backendApiUrl + "/vehicle-types";
            HttpEntity<String> request = new HttpEntity<>(new HttpHeaders()); // No token needed
            ResponseEntity<List<VehicleType>> vehicleTypeResponse = restTemplate.exchange(
                vehicleTypeUrl, HttpMethod.GET, request, new ParameterizedTypeReference<List<VehicleType>>(){}
            );
            List<VehicleType> vehicleTypes = vehicleTypeResponse.getBody();
            if (vehicleTypes == null || vehicleTypes.isEmpty()) {
                System.err.println("No vehicle types returned from " + vehicleTypeUrl);
                model.addAttribute("error", "No vehicle types available. Please try again later.");
            }

            // Fetch service packages
            String servicePackageUrl = backendApiUrl + "/service-packages";
            ResponseEntity<List<ServicePackage>> servicePackageResponse = restTemplate.exchange(
                servicePackageUrl, HttpMethod.GET, request, new ParameterizedTypeReference<List<ServicePackage>>(){}
            );
            List<ServicePackage> servicePackages = servicePackageResponse.getBody();
            if (servicePackages == null || servicePackages.isEmpty()) {
                System.err.println("No service packages returned from " + servicePackageUrl);
                model.addAttribute("error", "No service packages available. Please try again later.");
            }

            model.addAttribute("vehicleTypes", vehicleTypes != null ? vehicleTypes : Collections.emptyList());
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
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

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
}
