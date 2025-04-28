package com.frontend.controller;

import com.frontend.model.*;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class AdminController {

    @Value("${backend.api.url}")
    private String backendApiUrl;

    private final RestTemplate restTemplate;
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    public AdminController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    @GetMapping("/dashboard/admin")
    public String showDashboard(Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "No authentication token found. Please log in.");
            return "index";
        }

        try {
            String url = backendApiUrl + "/dashboard/admin";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<String> request = new HttpEntity<>(headers);

            DashboardData data = restTemplate.exchange(url, HttpMethod.GET, request, DashboardData.class).getBody();
            model.addAttribute("dashboardData", data);
            model.addAttribute("token", token);
            // Set profileName from the fetched data
            if (data != null && data.getProfileName() != null) {
                model.addAttribute("profileName", data.getProfileName());
            } else {
                model.addAttribute("profileName", "Admin User"); // Fallback
            }

        } catch (Exception e) {
            model.addAttribute("error", "Failed to load dashboard: " + e.getMessage());
            // Use mock data as a fallback
            addMockData(model);
        }

        return "admin-dashboard";
    }

    private void addMockData(Model model) {
        // Create advisors list and filter active ones
        List<Advisor> allAdvisors = Arrays.asList(
                new Advisor(1L, "New York", "NY Center", "alice", "alice@example.com", "1234567890", "pass123", true),
                new Advisor(2L, "Los Angeles", "LA Center", "bob", "bob@example.com", "0987654321", "pass456", false)
        );

        List<Advisor> activeAdvisors = allAdvisors.stream()
                .filter(Advisor::getActive)
                .collect(Collectors.toList());

        DashboardData dashboardData = new DashboardData(
                1, 1, 1, 1, "Admin User",
                Arrays.asList(
                        new VehicleDue(
                                "John Doe",
                                "Honda Civic",
                                "Sedan",
                                "Oil Change",
                                "Bangalore",
                                1L,
                                activeAdvisors,
                                new Date(),
                                new Date(),
                                "Requested"
                        )
                ),
                Arrays.asList(
                        new VehicleUnderService("Jane Smith", "Sedan", "Honda City", "Center A", "John Doe", "Engine Check", "In Progress")
                ),
                Arrays.asList(
                        new VehicleCompleted(1L, "Sam White", "SUV", "Hyundai Creta", "Center A", "John Doe", "Oil Change, Brake Check",
                                new Date(), "Completed", false, false)
                ),
                Arrays.asList(
                        new AdvisorRequest(1L, "Mark Advisor", "Wheel Alignment", "Jane Smith", "SUV", "Mahindra XUV500", "Center A", "Pending")
                )
        );

        model.addAttribute("dashboardData", dashboardData);
        model.addAttribute("profileName", dashboardData.getProfileName());
    }      
    private List<ServiceItem> fetchInventoryList(String token) {
        String url = backendApiUrl + "/inventory";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> rawResponse = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            logger.info("Raw JSON response from /api/inventory: {}", rawResponse.getBody());

            ResponseEntity<ServiceItem[]> response = restTemplate.exchange(url, HttpMethod.GET, request, ServiceItem[].class);
            return response.getBody() != null ? Arrays.asList(response.getBody()) : List.of();
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error fetching inventory: Status {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return List.of();
        } catch (Exception e) {
            logger.error("Error fetching inventory: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @GetMapping("/inventory")
    public String showInventory(Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "Please log in to access this page.");
            return "redirect:/";
        }

        model.addAttribute("inventoryList", fetchInventoryList(token));
        model.addAttribute("inventoryItem", new ServiceItem());
        model.addAttribute("formMode", "none");
        model.addAttribute("profileName", "Admin");
        return "inventory";
    }

    @GetMapping("/inventory/add")
    public String showAddInventoryForm(Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "Please log in to access this page.");
            return "redirect:/";
        }

        model.addAttribute("inventoryItem", new ServiceItem());
        model.addAttribute("formMode", "add");
        model.addAttribute("inventoryList", fetchInventoryList(token));
        model.addAttribute("profileName", "Admin");
        return "inventory";
    }

    @PostMapping("/inventory/add")
    public String addInventory(@ModelAttribute("inventoryItem") ServiceItem item, HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "Please log in to access this page.");
            return "redirect:/";
        }

        if (item.getWorkitems() == null || item.getWorkitems().trim().isEmpty()) {
            model.addAttribute("error", "Item name cannot be empty.");
            model.addAttribute("formMode", "add");
            model.addAttribute("inventoryList", fetchInventoryList(token));
            model.addAttribute("profileName", "Admin");
            return "inventory";
        }

        if (item.getQuantity() == null || item.getQuantity() < 0) {
            model.addAttribute("error", "Quantity must be a non-negative number.");
            model.addAttribute("formMode", "add");
            model.addAttribute("inventoryList", fetchInventoryList(token));
            model.addAttribute("profileName", "Admin");
            return "inventory";
        }

        if (item.getPrices() == null || item.getPrices() < 0) {
            model.addAttribute("error", "Price must be a non-negative number.");
            model.addAttribute("formMode", "add");
            model.addAttribute("inventoryList", fetchInventoryList(token));
            model.addAttribute("profileName", "Admin");
            return "inventory";
        }

        try {
            item.setLastUpdated(LocalDate.now());

            String url = backendApiUrl + "/inventory";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

            logger.info("Sending add inventory request: {}", item);
            HttpEntity<ServiceItem> request = new HttpEntity<>(item, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            logger.info("Add inventory response: Status {}, Body: {}", response.getStatusCode(), response.getBody());

            return "redirect:/inventory";
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error adding inventory: Status {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            model.addAttribute("error", "Failed to create inventory item: " + e.getResponseBodyAsString());
            model.addAttribute("formMode", "add");
            model.addAttribute("inventoryList", fetchInventoryList(token));
            model.addAttribute("profileName", "Admin");
            return "inventory";
        } catch (Exception e) {
            logger.error("Error adding inventory: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to create inventory item: " + e.getMessage());
            model.addAttribute("formMode", "add");
            model.addAttribute("inventoryList", fetchInventoryList(token));
            model.addAttribute("profileName", "Admin");
            return "inventory";
        }
    }

    @GetMapping("/inventory/edit/{id}")
    public String showEditInventory(@PathVariable Long id, Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "Please log in to access this page.");
            return "redirect:/";
        }

        try {
            String url = backendApiUrl + "/inventory/" + id;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<ServiceItem> response = restTemplate.exchange(url, HttpMethod.GET, request, ServiceItem.class);
            model.addAttribute("inventoryItem", response.getBody());
            model.addAttribute("formMode", "edit");
            model.addAttribute("inventoryList", fetchInventoryList(token));
            model.addAttribute("profileName", "Admin");
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error fetching inventory item for edit: Status {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            model.addAttribute("error", "Failed to fetch inventory item: " + e.getResponseBodyAsString());
            model.addAttribute("formMode", "none");
            model.addAttribute("inventoryList", fetchInventoryList(token));
            model.addAttribute("profileName", "Admin");
        } catch (Exception e) {
            logger.error("Error fetching inventory item for edit: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to fetch inventory item: " + e.getMessage());
            model.addAttribute("formMode", "none");
            model.addAttribute("inventoryList", fetchInventoryList(token));
            model.addAttribute("profileName", "Admin");
        }

        return "inventory";
    }

    @PostMapping("/inventory/edit/{id}")
    public String editInventory(@PathVariable Long id, @ModelAttribute("inventoryItem") ServiceItem item, HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "Please log in to access this page.");
            return "redirect:/";
        }

        if (item.getWorkitems() == null || item.getWorkitems().trim().isEmpty()) {
            model.addAttribute("error", "Item name cannot be empty.");
            model.addAttribute("formMode", "edit");
            model.addAttribute("inventoryList", fetchInventoryList(token));
            model.addAttribute("profileName", "Admin");
            return "inventory";
        }

        if (item.getQuantity() == null || item.getQuantity() < 0) {
            model.addAttribute("error", "Quantity must be a non-negative number.");
            model.addAttribute("formMode", "edit");
            model.addAttribute("inventoryList", fetchInventoryList(token));
            model.addAttribute("profileName", "Admin");
            return "inventory";
        }

        if (item.getPrices() == null || item.getPrices() < 0) {
            model.addAttribute("error", "Price must be a non-negative number.");
            model.addAttribute("formMode", "edit");
            model.addAttribute("inventoryList", fetchInventoryList(token));
            model.addAttribute("profileName", "Admin");
            return "inventory";
        }

        try {
            item.setLastUpdated(LocalDate.now()); // Update lastUpdated to current date

            String url = backendApiUrl + "/inventory/" + id;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

            logger.info("Sending edit inventory request: {}", item);
            HttpEntity<ServiceItem> request = new HttpEntity<>(item, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
            logger.info("Edit inventory response: Status {}, Body: {}", response.getStatusCode(), response.getBody());

            return "redirect:/inventory";
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error editing inventory: Status {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            model.addAttribute("error", "Failed to update inventory item: " + e.getResponseBodyAsString());
            model.addAttribute("formMode", "edit");
            model.addAttribute("inventoryList", fetchInventoryList(token));
            model.addAttribute("profileName", "Admin");
            return "inventory";
        } catch (Exception e) {
            logger.error("Error editing inventory: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to update inventory item: " + e.getMessage());
            model.addAttribute("formMode", "edit");
            model.addAttribute("inventoryList", fetchInventoryList(token));
            model.addAttribute("profileName", "Admin");
            return "inventory";
        }
    }

    @PostMapping("/inventory/delete/{id}")
    public String deleteInventory(@PathVariable Long id, HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "Please log in to access this page.");
            return "redirect:/";
        }

        try {
            String url = backendApiUrl + "/inventory/" + id;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<String> request = new HttpEntity<>(headers);

            logger.info("Sending delete inventory request for ID: {}", id);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
            logger.info("Delete inventory response: Status {}, Body: {}", response.getStatusCode(), response.getBody());

            return "redirect:/inventory";
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error deleting inventory: Status {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            model.addAttribute("error", "Failed to delete inventory item: " + e.getResponseBodyAsString());
            model.addAttribute("formMode", "none");
            model.addAttribute("inventoryList", fetchInventoryList(token));
            model.addAttribute("profileName", "Admin");
            return "inventory";
        } catch (Exception e) {
            logger.error("Error deleting inventory: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to delete inventory item: " + e.getMessage());
            model.addAttribute("formMode", "none");
            model.addAttribute("inventoryList", fetchInventoryList(token));
            model.addAttribute("profileName", "Admin");
            return "inventory";
        }
    }

    // SERVICE CENTER MODULE
    private String serviceCenterUrl() {
        return backendApiUrl + "/service-centers";
    }

    private List<ServiceCenter> fetchAll(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        HttpEntity<String> request = new HttpEntity<>(headers);
        ResponseEntity<ServiceCenter[]> response = restTemplate.exchange(serviceCenterUrl(), HttpMethod.GET, request, ServiceCenter[].class);

        return response.getBody() != null ? Arrays.asList(response.getBody()) : List.of();
    }

    @GetMapping("/service-centers")
    public String listCenters(Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null) return "redirect:/";

        model.addAttribute("serviceCenters", fetchAll(token));
        model.addAttribute("formMode", "none");
        return "service-center";
    }

    @GetMapping("/service-centers/add")
    public String showAddForm(Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null) return "redirect:/";

        model.addAttribute("serviceCenters", fetchAll(token));
        model.addAttribute("serviceCenter", new ServiceCenter());
        model.addAttribute("formMode", "add");
        return "service-center";
    }

    @PostMapping("/service-centers/add")
    public String doAdd(@ModelAttribute ServiceCenter serviceCenter, HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");

        if (token == null) {
            model.addAttribute("error", "Unauthorized: Please log in again.");
            return "redirect:/";
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<ServiceCenter> request = new HttpEntity<>(serviceCenter, headers);

            restTemplate.postForEntity(serviceCenterUrl(), request, Void.class);
            return "redirect:/service-centers";

        } catch (Exception e) {
            model.addAttribute("error", "Failed to create service center: " + e.getMessage());
            model.addAttribute("serviceCenter", serviceCenter);
            model.addAttribute("formMode", "add");
            return "service-center";
        }
    }


    @GetMapping("/service-centers/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null) return "redirect:/";

        model.addAttribute("serviceCenters", fetchAll(token));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> request = new HttpEntity<>(headers);

        ServiceCenter sc = restTemplate.exchange(serviceCenterUrl() + "/" + id, HttpMethod.GET, request, ServiceCenter.class).getBody();
        model.addAttribute("serviceCenter", sc);
        model.addAttribute("formMode", "edit");
        return "service-center";
    }

    @PostMapping("/service-centers/edit/{id}")
    public String doEdit(@PathVariable Long id, @ModelAttribute ServiceCenter serviceCenter, HttpSession session) {
        String token = (String) session.getAttribute("token");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ServiceCenter> request = new HttpEntity<>(serviceCenter, headers);
        restTemplate.exchange(serviceCenterUrl() + "/" + id, HttpMethod.PUT, request, Void.class);
        return "redirect:/service-centers";
    }

    @GetMapping("/service-centers/delete/{id}")
    public String doDelete(@PathVariable Long id, HttpSession session) {
        String token = (String) session.getAttribute("token");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> request = new HttpEntity<>(headers);

        restTemplate.exchange(serviceCenterUrl() + "/" + id, HttpMethod.DELETE, request, Void.class);
        return "redirect:/service-centers";
    }
}