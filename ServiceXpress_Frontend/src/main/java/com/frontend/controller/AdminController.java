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
 // VEHICLE TYPE MODULE
    private String vehicleTypeUrl() {
        return backendApiUrl + "/vehicle-type";
    }

    private List<VehicleType> fetchVehicleTypes(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> rawResponse = restTemplate.exchange(
                    vehicleTypeUrl(), HttpMethod.GET, request, String.class);
            logger.info("Raw JSON response from /api/vehicle-type: {}", rawResponse.getBody());

            ResponseEntity<VehicleType[]> response = restTemplate.exchange(
                    vehicleTypeUrl(), HttpMethod.GET, request, VehicleType[].class);
            return response.getBody() != null ? Arrays.asList(response.getBody()) : List.of();
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error fetching vehicle types: Status {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to fetch vehicle types: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            logger.error("Error fetching vehicle types: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch vehicle types: " + e.getMessage(), e);
        }
    }

    @GetMapping("/vehicle-type")
    public String listVehicleTypes(Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "Please log in to access this page.");
            return "redirect:/";
        }

        try {
            List<VehicleType> vehicleTypes = fetchVehicleTypes(token);
            model.addAttribute("vehicleTypes", vehicleTypes);
        } catch (Exception e) {
            model.addAttribute("error", "Failed to fetch vehicle types: " + e.getMessage());
        }
        model.addAttribute("vehicleType", new VehicleType());
        model.addAttribute("formMode", "none");
        return "vehicle-type";
    }

    @GetMapping("/vehicle-type/add")
    public String showAddVehicleTypeForm(Model model) {
        model.addAttribute("vehicleType", new VehicleType());
        model.addAttribute("formMode", "add");
        return "vehicle-type";
    }

    @PostMapping("/vehicle-type/add")
    public String addVehicleType(@ModelAttribute VehicleType vehicleType, HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "Please log in to access this page.");
            return "redirect:/";
        }

        if (vehicleType.getTypeName() == null || vehicleType.getTypeName().trim().isEmpty()) {
            model.addAttribute("error", "Vehicle Type name cannot be empty.");
            model.addAttribute("formMode", "add");
            try {
                model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
            } catch (Exception e) {
                model.addAttribute("error", model.asMap().getOrDefault("error", "") + "; Failed to fetch vehicle types: " + e.getMessage());
            }
            return "vehicle-type";
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

            logger.info("Sending add vehicle type request: {}", vehicleType);
            HttpEntity<VehicleType> request = new HttpEntity<>(vehicleType, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(vehicleTypeUrl(), request, String.class);
            logger.info("Add vehicle type response: {}", response.getBody());

            return "redirect:/vehicle-type";
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error adding vehicle type: Status {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            model.addAttribute("error", "Failed to create vehicle type: " + e.getResponseBodyAsString());
            model.addAttribute("formMode", "add");
            try {
                model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
            } catch (Exception ex) {
                model.addAttribute("error", model.asMap().getOrDefault("error", "") + "; Failed to fetch vehicle types: " + ex.getMessage());
            }
            return "vehicle-type";
        } catch (Exception e) {
            logger.error("Error adding vehicle type: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to create vehicle type: " + e.getMessage());
            model.addAttribute("formMode", "add");
            try {
                model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
            } catch (Exception ex) {
                model.addAttribute("error", model.asMap().getOrDefault("error", "") + "; Failed to fetch vehicle types: " + ex.getMessage());
            }
            return "vehicle-type";
        }
    }

    @GetMapping("/vehicle-type/edit/{id}")
    public String showEditVehicleTypeForm(@PathVariable Long id, Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "Please log in to access this page.");
            return "redirect:/";
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<VehicleType> response = restTemplate.exchange(
                    vehicleTypeUrl() + "/" + id, HttpMethod.GET, request, VehicleType.class);
            model.addAttribute("vehicleType", response.getBody());
            model.addAttribute("formMode", "edit");
            model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
        } catch (Exception e) {
            logger.error("Error fetching vehicle type for edit: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to fetch vehicle type: " + e.getMessage());
            model.addAttribute("formMode", "none");
            try {
                model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
            } catch (Exception ex) {
                model.addAttribute("error", model.asMap().getOrDefault("error", "") + "; Failed to fetch vehicle types: " + ex.getMessage());
            }
        }

        return "vehicle-type";
    }

    @PostMapping("/vehicle-type/edit/{id}")
    public String editVehicleType(@PathVariable Long id, @ModelAttribute VehicleType vehicleType, HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "Please log in to access this page.");
            return "redirect:/";
        }

        if (vehicleType.getTypeName() == null || vehicleType.getTypeName().trim().isEmpty()) {
            model.addAttribute("error", "Vehicle Type name cannot be empty.");
            model.addAttribute("formMode", "edit");
            try {
                model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
            } catch (Exception e) {
                model.addAttribute("error", model.asMap().getOrDefault("error", "") + "; Failed to fetch vehicle types: " + e.getMessage());
            }
            return "vehicle-type";
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

            logger.info("Sending edit vehicle type request: {}", vehicleType);
            HttpEntity<VehicleType> request = new HttpEntity<>(vehicleType, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    vehicleTypeUrl() + "/" + id, HttpMethod.PUT, request, String.class);
            logger.info("Edit vehicle type response: {}", response.getBody());

            return "redirect:/vehicle-type";
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error editing vehicle type: Status {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            model.addAttribute("error", "Failed to update vehicle type: " + e.getResponseBodyAsString());
            model.addAttribute("formMode", "edit");
            try {
                model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
            } catch (Exception ex) {
                model.addAttribute("error", model.asMap().getOrDefault("error", "") + "; Failed to fetch vehicle types: " + ex.getMessage());
            }
            return "vehicle-type";
        } catch (Exception e) {
            logger.error("Error editing vehicle type: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to update vehicle type: " + e.getMessage());
            model.addAttribute("formMode", "edit");
            try {
                model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
            } catch (Exception ex) {
                model.addAttribute("error", model.asMap().getOrDefault("error", "") + "; Failed to fetch vehicle types: " + ex.getMessage());
            }
            return "vehicle-type";
        }
    }

    @PostMapping("/vehicle-type/delete/{id}")
    public String deleteVehicleType(@PathVariable Long id, HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "Please log in to access this page.");
            return "redirect:/";
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<String> request = new HttpEntity<>(headers);

            logger.info("Sending delete vehicle type request for ID: {}", id);
            ResponseEntity<String> response = restTemplate.exchange(
                    vehicleTypeUrl() + "/" + id, HttpMethod.DELETE, request, String.class);
            logger.info("Delete vehicle type response: {}", response.getBody());

            return "redirect:/vehicle-type";
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error deleting vehicle type: Status {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            model.addAttribute("error", "Failed to delete vehicle type: " + e.getResponseBodyAsString());
            try {
                model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
            } catch (Exception ex) {
                model.addAttribute("error", model.asMap().getOrDefault("error", "") + "; Failed to fetch vehicle types: " + ex.getMessage());
            }
            return "vehicle-type";
        } catch (Exception e) {
            logger.error("Error deleting vehicle type: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to delete vehicle type: " + e.getMessage());
            try {
                model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
            } catch (Exception ex) {
                model.addAttribute("error", model.asMap().getOrDefault("error", "") + "; Failed to fetch vehicle types: " + ex.getMessage());
            }
            return "vehicle-type";
        }
    }
    
 // VEHICLE MODEL MODULE
    private String vehicleModelUrl() {
        return backendApiUrl + "/vehicle-models";
    }

    private List<VehicleModel> fetchVehicleModels(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> rawResponse = restTemplate.exchange(
                    vehicleModelUrl(), HttpMethod.GET, request, String.class);
            logger.info("Raw JSON response from /api/vehicle-models: {}", rawResponse.getBody());

            ResponseEntity<VehicleModel[]> response = restTemplate.exchange(
                    vehicleModelUrl(), HttpMethod.GET, request, VehicleModel[].class);
            return response.getBody() != null ? Arrays.asList(response.getBody()) : List.of();
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error fetching vehicle models: Status {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to fetch vehicle models: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            logger.error("Error fetching vehicle models: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch vehicle models: " + e.getMessage(), e);
        }
    }

    @GetMapping("/vehicle-model")
    public String listVehicleModels(Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "Please log in to access this page.");
            return "redirect:/";
        }

        try {
            model.addAttribute("vehicleModels", fetchVehicleModels(token));
            model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
        } catch (Exception e) {
            model.addAttribute("error", "Failed to fetch vehicle models: " + e.getMessage());
        }
        model.addAttribute("vehicleModel", new VehicleModel());
        model.addAttribute("formMode", "none");
        return "vehicle-model";
    }

    @GetMapping("/vehicle-model/add")
    public String showAddVehicleModelForm(Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "Please log in to access this page.");
            return "redirect:/";
        }

        model.addAttribute("vehicleModel", new VehicleModel());
        model.addAttribute("formMode", "add");
        try {
            model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
            model.addAttribute("vehicleModels", fetchVehicleModels(token));
        } catch (Exception e) {
            model.addAttribute("error", "Failed to fetch data: " + e.getMessage());
        }
        return "vehicle-model";
    }

    @PostMapping("/vehicle-model/add")
    public String addVehicleModel(@ModelAttribute VehicleModel vehicleModel, HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "Please log in to access this page.");
            return "redirect:/";
        }

        if (vehicleModel.getModelName() == null || vehicleModel.getModelName().trim().isEmpty()) {
            model.addAttribute("error", "Model name cannot be empty.");
            model.addAttribute("formMode", "add");
            try {
                model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
                model.addAttribute("vehicleModels", fetchVehicleModels(token));
            } catch (Exception e) {
                model.addAttribute("error", model.asMap().getOrDefault("error", "") + "; Failed to fetch data: " + e.getMessage());
            }
            return "vehicle-model";
        }

        if (vehicleModel.getVehicleType() == null || vehicleModel.getVehicleType().getId() == null) {
            model.addAttribute("error", "Vehicle type is required.");
            model.addAttribute("formMode", "add");
            try {
                model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
                model.addAttribute("vehicleModels", fetchVehicleModels(token));
            } catch (Exception e) {
                model.addAttribute("error", model.asMap().getOrDefault("error", "") + "; Failed to fetch data: " + e.getMessage());
            }
            return "vehicle-model";
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

            logger.info("Sending add vehicle model request: {}", vehicleModel);
            HttpEntity<VehicleModel> request = new HttpEntity<>(vehicleModel, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(vehicleModelUrl(), request, String.class);
            logger.info("Add vehicle model response: {}", response.getBody());

            return "redirect:/vehicle-model";
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error adding vehicle model: Status {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            model.addAttribute("error", "Failed to create vehicle model: " + e.getResponseBodyAsString());
            model.addAttribute("formMode", "add");
            try {
                model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
                model.addAttribute("vehicleModels", fetchVehicleModels(token));
            } catch (Exception ex) {
                model.addAttribute("error", model.asMap().getOrDefault("error", "") + "; Failed to fetch data: " + ex.getMessage());
            }
            return "vehicle-model";
        } catch (Exception e) {
            logger.error("Error adding vehicle model: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to create vehicle model: " + e.getMessage());
            model.addAttribute("formMode", "add");
            try {
                model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
                model.addAttribute("vehicleModels", fetchVehicleModels(token));
            } catch (Exception ex) {
                model.addAttribute("error", model.asMap().getOrDefault("error", "") + "; Failed to fetch data: " + ex.getMessage());
            }
            return "vehicle-model";
        }
    }

    @GetMapping("/vehicle-model/edit/{id}")
    public String showEditVehicleModelForm(@PathVariable Long id, Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "Please log in to access this page.");
            return "redirect:/";
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<VehicleModel> response = restTemplate.exchange(
                    vehicleModelUrl() + "/" + id, HttpMethod.GET, request, VehicleModel.class);
            model.addAttribute("vehicleModel", response.getBody());
            model.addAttribute("formMode", "edit");
            model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
            model.addAttribute("vehicleModels", fetchVehicleModels(token));
        } catch (Exception e) {
            logger.error("Error fetching vehicle model for edit: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to fetch vehicle model: " + e.getMessage());
            model.addAttribute("formMode", "none");
            try {
                model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
                model.addAttribute("vehicleModels", fetchVehicleModels(token));
            } catch (Exception ex) {
                model.addAttribute("error", model.asMap().getOrDefault("error", "") + "; Failed to fetch data: " + ex.getMessage());
            }
        }

        return "vehicle-model";
    }

    @PostMapping("/vehicle-model/edit/{id}")
    public String editVehicleModel(@PathVariable Long id, @ModelAttribute VehicleModel vehicleModel, HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "Please log in to access this page.");
            return "redirect:/";
        }

        if (vehicleModel.getModelName() == null || vehicleModel.getModelName().trim().isEmpty()) {
            model.addAttribute("error", "Model name cannot be empty.");
            model.addAttribute("formMode", "edit");
            try {
                model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
                model.addAttribute("vehicleModels", fetchVehicleModels(token));
            } catch (Exception e) {
                model.addAttribute("error", model.asMap().getOrDefault("error", "") + "; Failed to fetch data: " + e.getMessage());
            }
            return "vehicle-model";
        }

        if (vehicleModel.getVehicleType() == null || vehicleModel.getVehicleType().getId() == null) {
            model.addAttribute("error", "Vehicle type is required.");
            model.addAttribute("formMode", "edit");
            try {
                model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
                model.addAttribute("vehicleModels", fetchVehicleModels(token));
            } catch (Exception e) {
                model.addAttribute("error", model.asMap().getOrDefault("error", "") + "; Failed to fetch data: " + e.getMessage());
            }
            return "vehicle-model";
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

            logger.info("Sending edit vehicle model request: {}", vehicleModel);
            HttpEntity<VehicleModel> request = new HttpEntity<>(vehicleModel, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    vehicleModelUrl() + "/" + id, HttpMethod.PUT, request, String.class);
            logger.info("Edit vehicle model response: {}", response.getBody());

            return "redirect:/vehicle-model";
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error editing vehicle model: Status {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            model.addAttribute("error", "Failed to update vehicle model: " + e.getResponseBodyAsString());
            model.addAttribute("formMode", "edit");
            try {
                model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
                model.addAttribute("vehicleModels", fetchVehicleModels(token));
            } catch (Exception ex) {
                model.addAttribute("error", model.asMap().getOrDefault("error", "") + "; Failed to fetch data: " + ex.getMessage());
            }
            return "vehicle-model";
        } catch (Exception e) {
            logger.error("Error editing vehicle model: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to update vehicle model: " + e.getMessage());
            model.addAttribute("formMode", "edit");
            try {
                model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
                model.addAttribute("vehicleModels", fetchVehicleModels(token));
            } catch (Exception ex) {
                model.addAttribute("error", model.asMap().getOrDefault("error", "") + "; Failed to fetch data: " + ex.getMessage());
            }
            return "vehicle-model";
        }
    }

    @PostMapping("/vehicle-model/delete/{id}")
    public String deleteVehicleModel(@PathVariable Long id, HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "Please log in to access this page.");
            return "redirect:/";
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<String> request = new HttpEntity<>(headers);

            logger.info("Sending delete vehicle model request for ID: {}", id);
            ResponseEntity<String> response = restTemplate.exchange(
                    vehicleModelUrl() + "/" + id, HttpMethod.DELETE, request, String.class);
            logger.info("Delete vehicle model response: {}", response.getBody());

            return "redirect:/vehicle-model";
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error deleting vehicle model: Status {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            model.addAttribute("error", "Failed to delete vehicle model: " + e.getResponseBodyAsString());
            try {
                model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
                model.addAttribute("vehicleModels", fetchVehicleModels(token));
            } catch (Exception ex) {
                model.addAttribute("error", model.asMap().getOrDefault("error", "") + "; Failed to fetch data: " + ex.getMessage());
            }
            return "vehicle-model";
        } catch (Exception e) {
            logger.error("Error deleting vehicle model: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to delete vehicle model: " + e.getMessage());
            try {
                model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
                model.addAttribute("vehicleModels", fetchVehicleModels(token));
            } catch (Exception ex) {
                model.addAttribute("error", model.asMap().getOrDefault("error", "") + "; Failed to fetch data: " + ex.getMessage());
            }
            return "vehicle-model";
        }
    }
    
    private String servicePackageUrl() {
        return backendApiUrl + "/service-packages";
    }

    private List<ServicePackage> fetchServicePackages(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> rawResponse = restTemplate.exchange(
                    servicePackageUrl(), HttpMethod.GET, request, String.class);
            logger.info("Raw JSON response from /api/service-packages: {}", rawResponse.getBody());

            ResponseEntity<ServicePackage[]> response = restTemplate.exchange(
                    servicePackageUrl(), HttpMethod.GET, request, ServicePackage[].class);
            return response.getBody() != null ? Arrays.asList(response.getBody()) : List.of();
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error fetching service packages: Status {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return List.of();
        } catch (Exception e) {
            logger.error("Error fetching service packages: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @GetMapping("/service-package")
    public String listServicePackages(Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "Please log in to access this page.");
            return "redirect:/";
        }

        model.addAttribute("servicePackages", fetchServicePackages(token));
        model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
        model.addAttribute("servicePackage", new ServicePackage());
        model.addAttribute("formMode", "none");
        return "service-package";
    }

    @GetMapping("/service-package/add")
    public String showAddServicePackageForm(Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "Please log in to access this page.");
            return "redirect:/";
        }

        model.addAttribute("servicePackage", new ServicePackage());
        model.addAttribute("formMode", "add");
        model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
        model.addAttribute("servicePackages", fetchServicePackages(token));
        return "service-package";
    }

    @PostMapping("/service-package/add")
    public String addServicePackage(@ModelAttribute ServicePackage servicePackage, HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "Please log in to access this page.");
            return "redirect:/";
        }

        if (servicePackage.getPackageName() == null || servicePackage.getPackageName().trim().isEmpty()) {
            model.addAttribute("error", "Package name is required and cannot be empty.");
            model.addAttribute("formMode", "add");
            model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
            model.addAttribute("servicePackages", fetchServicePackages(token));
            return "service-package";
        }

        if (servicePackage.getVehicleType() == null || servicePackage.getVehicleType().getId() == null) {
            model.addAttribute("error", "Please select a valid vehicle type.");
            model.addAttribute("formMode", "add");
            model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
            model.addAttribute("servicePackages", fetchServicePackages(token));
            return "service-package";
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

            logger.info("Sending add service package request: {}", servicePackage);
            HttpEntity<ServicePackage> request = new HttpEntity<>(servicePackage, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(servicePackageUrl(), request, String.class);
            logger.info("Add service package response: Status {}, Body: {}", response.getStatusCode(), response.getBody());

            return "redirect:/service-package";
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error adding service package: Status {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            model.addAttribute("error", "Failed to create service package: " + e.getResponseBodyAsString());
            model.addAttribute("formMode", "add");
            model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
            model.addAttribute("servicePackages", fetchServicePackages(token));
            return "service-package";
        } catch (Exception e) {
            logger.error("Error adding service package: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to create service package: " + e.getMessage());
            model.addAttribute("formMode", "add");
            model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
            model.addAttribute("servicePackages", fetchServicePackages(token));
            return "service-package";
        }
    }

    @GetMapping("/service-package/edit/{id}")
    public String showEditServicePackageForm(@PathVariable Long id, Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "Please log in to access this page.");
            return "redirect:/";
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<ServicePackage> response = restTemplate.exchange(
                    servicePackageUrl() + "/" + id, HttpMethod.GET, request, ServicePackage.class);
            model.addAttribute("servicePackage", response.getBody());
            model.addAttribute("formMode", "edit");
            model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
            model.addAttribute("servicePackages", fetchServicePackages(token));
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error fetching service package for edit: Status {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            model.addAttribute("error", "Failed to fetch service package: " + e.getResponseBodyAsString());
            model.addAttribute("formMode", "none");
            model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
            model.addAttribute("servicePackages", fetchServicePackages(token));
        } catch (Exception e) {
            logger.error("Error fetching service package for edit: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to fetch service package: " + e.getMessage());
            model.addAttribute("formMode", "none");
            model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
            model.addAttribute("servicePackages", fetchServicePackages(token));
        }

        return "service-package";
    }

    @PostMapping("/service-package/edit/{id}")
    public String editServicePackage(@PathVariable Long id, @ModelAttribute ServicePackage servicePackage, HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "Please log in to access this page.");
            return "redirect:/";
        }

        if (servicePackage.getPackageName() == null || servicePackage.getPackageName().trim().isEmpty()) {
            model.addAttribute("error", "Package name is required and cannot be empty.");
            model.addAttribute("formMode", "edit");
            model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
            model.addAttribute("servicePackages", fetchServicePackages(token));
            return "service-package";
        }

        if (servicePackage.getVehicleType() == null || servicePackage.getVehicleType().getId() == null) {
            model.addAttribute("error", "Please select a valid vehicle type.");
            model.addAttribute("formMode", "edit");
            model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
            model.addAttribute("servicePackages", fetchServicePackages(token));
            return "service-package";
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

            logger.info("Sending edit service package request: {}", servicePackage);
            HttpEntity<ServicePackage> request = new HttpEntity<>(servicePackage, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    servicePackageUrl() + "/" + id, HttpMethod.PUT, request, String.class);
            logger.info("Edit service package response: Status {}, Body: {}", response.getStatusCode(), response.getBody());

            return "redirect:/service-package";
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error editing service package: Status {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            model.addAttribute("error", "Failed to update service package: " + e.getResponseBodyAsString());
            model.addAttribute("formMode", "edit");
            model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
            model.addAttribute("servicePackages", fetchServicePackages(token));
            return "service-package";
        } catch (Exception e) {
            logger.error("Error editing service package: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to update service package: " + e.getMessage());
            model.addAttribute("formMode", "edit");
            model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
            model.addAttribute("servicePackages", fetchServicePackages(token));
            return "service-package";
        }
    }

    @PostMapping("/service-package/delete/{id}")
    public String deleteServicePackage(@PathVariable Long id, HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "Please log in to access this page.");
            return "redirect:/";
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<String> request = new HttpEntity<>(headers);

            logger.info("Sending delete service package request for ID: {}", id);
            ResponseEntity<String> response = restTemplate.exchange(
                    servicePackageUrl() + "/" + id, HttpMethod.DELETE, request, String.class);
            logger.info("Delete service package response: Status {}, Body: {}", response.getStatusCode(), response.getBody());

            return "redirect:/service-package";
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error deleting service package: Status {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            model.addAttribute("error", "Failed to delete service package: " + e.getResponseBodyAsString());
            model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
            model.addAttribute("servicePackages", fetchServicePackages(token));
            return "service-package";
        } catch (Exception e) {
            logger.error("Error deleting service package: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to delete service package: " + e.getMessage());
            model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
            model.addAttribute("servicePackages", fetchServicePackages(token));
            return "service-package";
        }
    }
    
    private List<Advisor> fetchAdvisorsList(String token) {
        String url = backendApiUrl + "/advisors";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> rawResponse = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            logger.info("Raw JSON response from /api/advisors: {}", rawResponse.getBody());

            ResponseEntity<Advisor[]> response = restTemplate.exchange(url, HttpMethod.GET, request, Advisor[].class);
            return response.getBody() != null ? Arrays.asList(response.getBody()) : List.of();
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error fetching advisors: Status {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return List.of();
        } catch (Exception e) {
            logger.error("Error fetching advisors: {}", e.getMessage(), e);
            return List.of();
        }
    }

    @GetMapping("/advisors")
    public String showAdvisors(Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "Please log in to access this page.");
            return "redirect:/";
        }

        model.addAttribute("advisorsList", fetchAdvisorsList(token));
        model.addAttribute("advisor", new Advisor());
        model.addAttribute("formMode", "none");
        model.addAttribute("profileName", "Admin");
        return "advisors";
    }

    @GetMapping("/advisors/add")
    public String showAddAdvisorForm(Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "Please log in to access this page.");
            return "redirect:/";
        }

        model.addAttribute("advisor", new Advisor());
        model.addAttribute("formMode", "add");
        model.addAttribute("advisorsList", fetchAdvisorsList(token));
        model.addAttribute("profileName", "Admin");
        return "advisors";
    }

    @PostMapping("/advisors/add")
    public String addAdvisor(@ModelAttribute("advisor") Advisor advisor, HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "Please log in to access this page.");
            return "redirect:/";
        }

        if (advisor.getCityName() == null || advisor.getCityName().trim().isEmpty()) {
            model.addAttribute("error", "City name cannot be empty.");
            model.addAttribute("formMode", "add");
            model.addAttribute("advisorsList", fetchAdvisorsList(token));
            model.addAttribute("profileName", "Admin");
            return "advisors";
        }

        if (advisor.getCenterName() == null || advisor.getCenterName().trim().isEmpty()) {
            model.addAttribute("error", "Center name cannot be empty.");
            model.addAttribute("formMode", "add");
            model.addAttribute("advisorsList", fetchAdvisorsList(token));
            model.addAttribute("profileName", "Admin");
            return "advisors";
        }

        if (advisor.getUsername() == null || advisor.getUsername().trim().isEmpty()) {
            model.addAttribute("error", "Username cannot be empty.");
            model.addAttribute("formMode", "add");
            model.addAttribute("advisorsList", fetchAdvisorsList(token));
            model.addAttribute("profileName", "Admin");
            return "advisors";
        }

        if (advisor.getEmail() == null || advisor.getEmail().trim().isEmpty()) {
            model.addAttribute("error", "Email cannot be empty.");
            model.addAttribute("formMode", "add");
            model.addAttribute("advisorsList", fetchAdvisorsList(token));
            model.addAttribute("profileName", "Admin");
            return "advisors";
        }

        if (advisor.getPassword() == null || advisor.getPassword().trim().isEmpty()) {
            model.addAttribute("error", "Password cannot be empty.");
            model.addAttribute("formMode", "add");
            model.addAttribute("advisorsList", fetchAdvisorsList(token));
            model.addAttribute("profileName", "Admin");
            return "advisors";
        }

        try {
            advisor.setActive(true); // Ensure the advisor is active

            String url = backendApiUrl + "/advisors";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

            logger.info("Sending add advisor request: {}", advisor);
            HttpEntity<Advisor> request = new HttpEntity<>(advisor, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            logger.info("Add advisor response: Status {}, Body: {}", response.getStatusCode(), response.getBody());

            return "redirect:/advisors";
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error adding advisor: Status {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            model.addAttribute("error", "Failed to create advisor: " + e.getResponseBodyAsString());
            model.addAttribute("formMode", "add");
            model.addAttribute("advisorsList", fetchAdvisorsList(token));
            model.addAttribute("profileName", "Admin");
            return "advisors";
        } catch (Exception e) {
            logger.error("Error adding advisor: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to create advisor: " + e.getMessage());
            model.addAttribute("formMode", "add");
            model.addAttribute("advisorsList", fetchAdvisorsList(token));
            model.addAttribute("profileName", "Admin");
            return "advisors";
        }
    }

    @GetMapping("/advisors/edit/{id}")
    public String showEditAdvisor(@PathVariable Long id, Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "Please log in to access this page.");
            return "redirect:/";
        }

        try {
            String url = backendApiUrl + "/advisors/" + id;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<Advisor> response = restTemplate.exchange(url, HttpMethod.GET, request, Advisor.class);
            model.addAttribute("advisor", response.getBody());
            model.addAttribute("formMode", "edit");
            model.addAttribute("advisorsList", fetchAdvisorsList(token));
            model.addAttribute("profileName", "Admin");
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error fetching advisor for edit: Status {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            model.addAttribute("error", "Failed to fetch advisor: " + e.getResponseBodyAsString());
            model.addAttribute("formMode", "none");
            model.addAttribute("advisorsList", fetchAdvisorsList(token));
            model.addAttribute("profileName", "Admin");
        } catch (Exception e) {
            logger.error("Error fetching advisor for edit: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to fetch advisor: " + e.getMessage());
            model.addAttribute("formMode", "none");
            model.addAttribute("advisorsList", fetchAdvisorsList(token));
            model.addAttribute("profileName", "Admin");
        }

        return "advisors";
    }

    @PostMapping("/advisors/edit/{id}")
    public String editAdvisor(@PathVariable Long id, @ModelAttribute("advisor") Advisor advisor, HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "Please log in to access this page.");
            return "redirect:/";
        }

        if (advisor.getCityName() == null || advisor.getCityName().trim().isEmpty()) {
            model.addAttribute("error", "City name cannot be empty.");
            model.addAttribute("formMode", "edit");
            model.addAttribute("advisorsList", fetchAdvisorsList(token));
            model.addAttribute("profileName", "Admin");
            return "advisors";
        }

        if (advisor.getCenterName() == null || advisor.getCenterName().trim().isEmpty()) {
            model.addAttribute("error", "Center name cannot be empty.");
            model.addAttribute("formMode", "edit");
            model.addAttribute("advisorsList", fetchAdvisorsList(token));
            model.addAttribute("profileName", "Admin");
            return "advisors";
        }

        if (advisor.getUsername() == null || advisor.getUsername().trim().isEmpty()) {
            model.addAttribute("error", "Username cannot be empty.");
            model.addAttribute("formMode", "edit");
            model.addAttribute("advisorsList", fetchAdvisorsList(token));
            model.addAttribute("profileName", "Admin");
            return "advisors";
        }

        if (advisor.getEmail() == null || advisor.getEmail().trim().isEmpty()) {
            model.addAttribute("error", "Email cannot be empty.");
            model.addAttribute("formMode", "edit");
            model.addAttribute("advisorsList", fetchAdvisorsList(token));
            model.addAttribute("profileName", "Admin");
            return "advisors";
        }

        if (advisor.getPassword() == null || advisor.getPassword().trim().isEmpty()) {
            model.addAttribute("error", "Password cannot be empty.");
            model.addAttribute("formMode", "edit");
            model.addAttribute("advisorsList", fetchAdvisorsList(token));
            model.addAttribute("profileName", "Admin");
            return "advisors";
        }

        try {
            advisor.setActive(true); // Ensure the advisor remains active after edit

            String url = backendApiUrl + "/advisors/" + id;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

            logger.info("Sending edit advisor request: {}", advisor);
            HttpEntity<Advisor> request = new HttpEntity<>(advisor, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
            logger.info("Edit advisor response: Status {}, Body: {}", response.getStatusCode(), response.getBody());

            return "redirect:/advisors";
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error editing advisor: Status {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            model.addAttribute("error", "Failed to update advisor: " + e.getResponseBodyAsString());
            model.addAttribute("formMode", "edit");
            model.addAttribute("advisorsList", fetchAdvisorsList(token));
            model.addAttribute("profileName", "Admin");
            return "advisors";
        } catch (Exception e) {
            logger.error("Error editing advisor: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to update advisor: " + e.getMessage());
            model.addAttribute("formMode", "edit");
            model.addAttribute("advisorsList", fetchAdvisorsList(token));
            model.addAttribute("profileName", "Admin");
            return "advisors";
        }
    }

    @PostMapping("/advisors/delete/{id}")
    public String deleteAdvisor(@PathVariable Long id, HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "Please log in to access this page.");
            return "redirect:/";
        }

        try {
            String url = backendApiUrl + "/advisors/" + id;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<String> request = new HttpEntity<>(headers);

            logger.info("Sending delete advisor request for ID: {}", id);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);
            logger.info("Delete advisor response: Status {}, Body: {}", response.getStatusCode(), response.getBody());

            return "redirect:/advisors";
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error deleting advisor: Status {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            model.addAttribute("error", "Failed to deactivate advisor: " + e.getResponseBodyAsString());
            model.addAttribute("formMode", "none");
            model.addAttribute("advisorsList", fetchAdvisorsList(token));
            model.addAttribute("profileName", "Admin");
            return "advisors";
        } catch (Exception e) {
            logger.error("Error deleting advisor: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to deactivate advisor: " + e.getMessage());
            model.addAttribute("formMode", "none");
            model.addAttribute("advisorsList", fetchAdvisorsList(token));
            model.addAttribute("profileName", "Admin");
            return "advisors";
        }
    }
   
 // CUSTOMER MODULE
    private String customerUrl() {
        return backendApiUrl + "/customers";
    }

    private List<Customer> fetchCustomersList(String token, boolean active) {
        String url = active ? customerUrl() + "/active" : customerUrl() + "/inactive";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> rawResponse = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            logger.info("Raw JSON response from /api/customers/{}: {}", active ? "active" : "inactive", rawResponse.getBody());

            ResponseEntity<Customer[]> response = restTemplate.exchange(url, HttpMethod.GET, request, Customer[].class);
            return response.getBody() != null ? Arrays.asList(response.getBody()) : List.of();
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error fetching customers (active={}): Status {}, Response: {}", active, e.getStatusCode(), e.getResponseBodyAsString());
            return List.of();
        } catch (Exception e) {
            logger.error("Error fetching customers (active={}): {}", active, e.getMessage(), e);
            return List.of();
        }
    }

    @GetMapping("/customers")
    public String showCustomers(Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "Please log in to access this page.");
            return "redirect:/";
        }

        model.addAttribute("token", token);
        model.addAttribute("backendApiUrl", backendApiUrl);
        model.addAttribute("customer", new Customer());
        model.addAttribute("formMode", "none");
        model.addAttribute("profileName", "Admin");
        return "customers";
    }

    @GetMapping("/customers/add")
    public String showAddCustomerForm(Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "Please log in to access this page.");
            return "redirect:/";
        }

        model.addAttribute("customer", new Customer());
        model.addAttribute("formMode", "add");
        model.addAttribute("token", token);
        model.addAttribute("backendApiUrl", backendApiUrl);
        model.addAttribute("profileName", "Admin");
        return "customers";
    }

    @PostMapping("/customers/add")
    public String addCustomer(@ModelAttribute("customer") Customer customer, HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "Please log in to access this page.");
            return "redirect:/";
        }

        // Validation
        if (customer.getUsername() == null || customer.getUsername().trim().isEmpty()) {
            model.addAttribute("error", "Username cannot be empty.");
            model.addAttribute("formMode", "add");
            model.addAttribute("token", token);
            model.addAttribute("backendApiUrl", backendApiUrl);
            model.addAttribute("profileName", "Admin");
            return "customers";
        }

        if (customer.getEmail() == null || customer.getEmail().trim().isEmpty()) {
            model.addAttribute("error", "Email cannot be empty.");
            model.addAttribute("formMode", "add");
            model.addAttribute("token", token);
            model.addAttribute("backendApiUrl", backendApiUrl);
            model.addAttribute("profileName", "Admin");
            return "customers";
        }

        if (customer.getPhoneNumber() == null || customer.getPhoneNumber().trim().isEmpty()) {
            model.addAttribute("error", "Phone number cannot be empty.");
            model.addAttribute("formMode", "add");
            model.addAttribute("token", token);
            model.addAttribute("backendApiUrl", backendApiUrl);
            model.addAttribute("profileName", "Admin");
            return "customers";
        }

        if (customer.getPassword() == null || customer.getPassword().trim().isEmpty()) {
            model.addAttribute("error", "Password cannot be empty.");
            model.addAttribute("formMode", "add");
            model.addAttribute("token", token);
            model.addAttribute("backendApiUrl", backendApiUrl);
            model.addAttribute("profileName", "Admin");
            return "customers";
        }

        try {
            customer.setActive(true); // New customers are active by default

            String url = customerUrl();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

            logger.info("Sending add customer request: {}", customer);
            HttpEntity<Customer> request = new HttpEntity<>(customer, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            logger.info("Add customer response: Status {}, Body: {}", response.getStatusCode(), response.getBody());

            return "redirect:/customers";
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error adding customer: Status {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            model.addAttribute("error", "Failed to create customer: " + e.getResponseBodyAsString());
            model.addAttribute("formMode", "add");
            model.addAttribute("token", token);
            model.addAttribute("backendApiUrl", backendApiUrl);
            model.addAttribute("profileName", "Admin");
            return "customers";
        } catch (Exception e) {
            logger.error("Error adding customer: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to create customer: " + e.getMessage());
            model.addAttribute("formMode", "add");
            model.addAttribute("token", token);
            model.addAttribute("backendApiUrl", backendApiUrl);
            model.addAttribute("profileName", "Admin");
            return "customers";
        }
    }

    @GetMapping("/customers/edit/{id}")
    public String showEditCustomer(@PathVariable Long id, Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "Please log in to access this page.");
            return "redirect:/";
        }

        try {
            String url = customerUrl() + "/" + id;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<Customer> response = restTemplate.exchange(url, HttpMethod.GET, request, Customer.class);
            model.addAttribute("customer", response.getBody());
            model.addAttribute("formMode", "edit");
            model.addAttribute("token", token);
            model.addAttribute("backendApiUrl", backendApiUrl);
            model.addAttribute("profileName", "Admin");
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error fetching customer for edit: Status {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            model.addAttribute("error", "Failed to fetch customer: " + e.getResponseBodyAsString());
            model.addAttribute("formMode", "none");
            model.addAttribute("token", token);
            model.addAttribute("backendApiUrl", backendApiUrl);
            model.addAttribute("profileName", "Admin");
        } catch (Exception e) {
            logger.error("Error fetching customer for edit: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to fetch customer: " + e.getMessage());
            model.addAttribute("formMode", "none");
            model.addAttribute("token", token);
            model.addAttribute("backendApiUrl", backendApiUrl);
            model.addAttribute("profileName", "Admin");
        }

        return "customers";
    }

    @PostMapping("/customers/edit/{id}")
    public String editCustomer(@PathVariable Long id, @ModelAttribute("customer") Customer customer, HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "Please log in to access this page.");
            return "redirect:/";
        }

        // Validation
        if (customer.getUsername() == null || customer.getUsername().trim().isEmpty()) {
            model.addAttribute("error", "Username cannot be empty.");
            model.addAttribute("formMode", "edit");
            model.addAttribute("token", token);
            model.addAttribute("backendApiUrl", backendApiUrl);
            model.addAttribute("profileName", "Admin");
            return "customers";
        }

        if (customer.getEmail() == null || customer.getEmail().trim().isEmpty()) {
            model.addAttribute("error", "Email cannot be empty.");
            model.addAttribute("formMode", "edit");
            model.addAttribute("token", token);
            model.addAttribute("backendApiUrl", backendApiUrl);
            model.addAttribute("profileName", "Admin");
            return "customers";
        }

        if (customer.getPhoneNumber() == null || customer.getPhoneNumber().trim().isEmpty()) {
            model.addAttribute("error", "Phone number cannot be empty.");
            model.addAttribute("formMode", "edit");
            model.addAttribute("token", token);
            model.addAttribute("backendApiUrl", backendApiUrl);
            model.addAttribute("profileName", "Admin");
            return "customers";
        }

        try {
            String url = customerUrl() + "/" + id;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

            logger.info("Sending edit customer request: {}", customer);
            HttpEntity<Customer> request = new HttpEntity<>(customer, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
            logger.info("Edit customer response: Status {}, Body: {}", response.getStatusCode(), response.getBody());

            return "redirect:/customers";
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error editing customer: Status {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            model.addAttribute("error", "Failed to update customer: " + e.getResponseBodyAsString());
            model.addAttribute("formMode", "edit");
            model.addAttribute("token", token);
            model.addAttribute("backendApiUrl", backendApiUrl);
            model.addAttribute("profileName", "Admin");
            return "customers";
        } catch (Exception e) {
            logger.error("Error editing customer: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to update customer: " + e.getMessage());
            model.addAttribute("formMode", "edit");
            model.addAttribute("token", token);
            model.addAttribute("backendApiUrl", backendApiUrl);
            model.addAttribute("profileName", "Admin");
            return "customers";
        }
    }

    @PostMapping("/customers/deactivate/{id}")
    public String deactivateCustomer(@PathVariable Long id, HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "Please log in to access this page.");
            return "redirect:/";
        }

        try {
            String url = customerUrl() + "/" + id + "/deactivate";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<String> request = new HttpEntity<>(headers);

            logger.info("Sending deactivate customer request for ID: {}", id);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
            logger.info("Deactivate customer response: Status {}, Body: {}", response.getStatusCode(), response.getBody());

            return "redirect:/customers";
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error deactivating customer: Status {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            model.addAttribute("error", "Failed to deactivate customer: " + e.getResponseBodyAsString());
            model.addAttribute("formMode", "none");
            model.addAttribute("token", token);
            model.addAttribute("backendApiUrl", backendApiUrl);
            model.addAttribute("profileName", "Admin");
            return "customers";
        } catch (Exception e) {
            logger.error("Error deactivating customer: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to deactivate customer: " + e.getMessage());
            model.addAttribute("formMode", "none");
            model.addAttribute("token", token);
            model.addAttribute("backendApiUrl", backendApiUrl);
            model.addAttribute("profileName", "Admin");
            return "customers";
        }
    }

    @PostMapping("/customers/reactivate/{id}")
    public String reactivateCustomer(@PathVariable Long id, HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "Please log in to access this page.");
            return "redirect:/";
        }

        try {
            String url = customerUrl() + "/" + id + "/reactivate";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            HttpEntity<String> request = new HttpEntity<>(headers);

            logger.info("Sending reactivate customer request for ID: {}", id);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
            logger.info("Reactivate customer response: Status {}, Body: {}", response.getStatusCode(), response.getBody());

            return "redirect:/customers";
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error reactivating customer: Status {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            model.addAttribute("error", "Failed to reactivate customer: " + e.getResponseBodyAsString());
            model.addAttribute("formMode", "none");
            model.addAttribute("token", token);
            model.addAttribute("backendApiUrl", backendApiUrl);
            model.addAttribute("profileName", "Admin");
            return "customers";
        } catch (Exception e) {
            logger.error("Error reactivating customer: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to reactivate customer: " + e.getMessage());
            model.addAttribute("formMode", "none");
            model.addAttribute("token", token);
            model.addAttribute("backendApiUrl", backendApiUrl);
            model.addAttribute("profileName", "Admin");
            return "customers";
        }
    }
    
    // LOGOUT
    @GetMapping("/admin/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

}