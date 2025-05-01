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
            return "redirect:/login";
        }
        try {
            String url = backendApiUrl + "/dashboard/admin";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<DashboardDataDTO> response = restTemplate.exchange(url, HttpMethod.GET, request, DashboardDataDTO.class);
            DashboardDataDTO data = response.getBody();
            if (data == null) {
                model.addAttribute("error", "No dashboard data received from backend.");
            } else {
                model.addAttribute("dashboardData", data);
                model.addAttribute("token", token);
            }
        } catch (Exception e) {
            logger.error("Failed to load dashboard: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to load dashboard: " + e.getMessage());
        }
        return "admin-dashboard";
    }

    @PostMapping("/dashboard/assign-advisor")
    @ResponseBody
    public ResponseEntity<String> assignAdvisor(@RequestBody Long advisorId, @RequestParam Long bookingId, HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null || token.isEmpty()) {
            logger.warn("No authentication token found in session for bookingId: {}", bookingId);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No authentication token found. Please log in.");
        }
        try {
            String url = backendApiUrl + "/admin/bookings/" + bookingId + "/assign-advisor";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(token);
            HttpEntity<Long> httpEntity = new HttpEntity<>(advisorId, headers);
            ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, Void.class);
            logger.info("Advisor {} assigned to booking {}", advisorId, bookingId);
            return ResponseEntity.ok("Advisor assigned successfully");
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error assigning advisor: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).body("Error assigning advisor: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            logger.error("Unexpected error assigning advisor: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Unexpected error: " + e.getMessage());
        }
    }
    
    
    private List<ServiceItem> fetchInventoryList(String token) {
        String url = backendApiUrl + "/inventory";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> rawResponse = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            ResponseEntity<ServiceItem[]> response = restTemplate.exchange(url, HttpMethod.GET, request, ServiceItem[].class);
            List<ServiceItem> inventoryList = response.getBody() != null ? Arrays.asList(response.getBody()) : List.of();
            return inventoryList;
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error fetching inventory: Status {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return Arrays.asList(
                new ServiceItem(1L, "Oil Change", 50, 29.99, LocalDate.parse("2025-04-28")),
                new ServiceItem(2L, "Tire Rotation", 20, 19.99, LocalDate.parse("2025-04-28")),
                new ServiceItem(3L, "Brake Pad Replacement", 10, 49.99, LocalDate.parse("2025-04-28")),
                new ServiceItem(4L, "Bike Chain", 52, 200.0, LocalDate.parse("2025-04-28"))
            );
        } catch (Exception e) {
            logger.error("Error fetching inventory: {}", e.getMessage(), e);
            return Arrays.asList(
                new ServiceItem(1L, "Oil Change", 50, 29.99, LocalDate.parse("2025-04-28")),
                new ServiceItem(2L, "Tire Rotation", 20, 19.99, LocalDate.parse("2025-04-28")),
                new ServiceItem(3L, "Brake Pad Replacement", 10, 49.99, LocalDate.parse("2025-04-28")),
                new ServiceItem(4L, "Bike Chain", 52, 200.0, LocalDate.parse("2025-04-28"))
            );
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

        if (item.getQuantity() == 0 || item.getQuantity() < 0) {
            model.addAttribute("error", "Quantity must be a non-negative number.");
            model.addAttribute("formMode", "add");
            model.addAttribute("inventoryList", fetchInventoryList(token));
            model.addAttribute("profileName", "Admin");
            return "inventory";
        }

        if (item.getPrices() == 0 || item.getPrices() < 0) {
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

            HttpEntity<ServiceItem> request = new HttpEntity<>(item, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

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

        if (item.getQuantity() == 0 || item.getQuantity() < 0) {
            model.addAttribute("error", "Quantity must be a non-negative number.");
            model.addAttribute("formMode", "edit");
            model.addAttribute("inventoryList", fetchInventoryList(token));
            model.addAttribute("profileName", "Admin");
            return "inventory";
        }

        if (item.getPrices() == 0 || item.getPrices() < 0) {
            model.addAttribute("error", "Price must be a non-negative number.");
            model.addAttribute("formMode", "edit");
            model.addAttribute("inventoryList", fetchInventoryList(token));
            model.addAttribute("profileName", "Admin");
            return "inventory";
        }

        try {
            item.setLastUpdated(LocalDate.now());

            String url = backendApiUrl + "/inventory/" + id;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

            HttpEntity<ServiceItem> request = new HttpEntity<>(item, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, request, String.class);

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

            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);

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
        return backendApiUrl + "/vehicle-types";
    }

    private List<VehicleType> fetchVehicleTypes(String token) {
        if (token == null || token.trim().isEmpty()) {
            logger.error("No JWT token found in session.");
            throw new RuntimeException("No authentication token found. Please log in again.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            logger.info("Fetching vehicle types with token: {}", token);
            ResponseEntity<VehicleTypeDTO[]> response = restTemplate.exchange(
                    vehicleTypeUrl(), HttpMethod.GET, request, VehicleTypeDTO[].class);
            logger.info("Successfully fetched vehicle types: {}", response.getBody());
            if (response.getBody() == null) {
                return List.of();
            }

            // Map VehicleTypeDTO to VehicleType
            return Arrays.stream(response.getBody())
                    .map(dto -> new VehicleType(dto.getId(), dto.getName()))
                    .collect(Collectors.toList());
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error fetching vehicle types: Status {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                logger.warn("Token might be invalid or expired, redirecting to login.");
                throw new RuntimeException("Authentication failed. Please log in again.");
            }
            throw new RuntimeException("Failed to fetch vehicle types: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            logger.error("Error fetching vehicle types: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch vehicle types: " + e.getMessage(), e);
        }
    }

    @GetMapping("/vehicle-type")
    public String listVehicleTypes(Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");
        logger.info("Token retrieved from session: {}", token);
        if (token == null) {
            logger.warn("No token found in session. Redirecting to login.");
            model.addAttribute("error", "Please log in to access this page.");
            return "redirect:/";
        }

        try {
            List<VehicleType> vehicleTypes = fetchVehicleTypes(token);
            model.addAttribute("vehicleTypes", vehicleTypes);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                logger.error("Invalid or expired token. Redirecting to logout.");
                session.invalidate(); // Invalidate session on 403
                return "redirect:/logout";
            }
            logger.error("Error in listVehicleTypes: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to fetch vehicle types: " + e.getMessage());
            model.addAttribute("vehicleTypes", Collections.emptyList()); // Fallback to empty list
        } catch (Exception e) {
            logger.error("Error in listVehicleTypes: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to fetch vehicle types: " + e.getMessage());
            model.addAttribute("vehicleTypes", Collections.emptyList()); // Fallback to empty list
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

        if (vehicleType.getName() == null || vehicleType.getName().trim().isEmpty()) {
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

        if (vehicleType.getName() == null || vehicleType.getName().trim().isEmpty()) {
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

            ResponseEntity<VehicleModelDTO[]> response = restTemplate.exchange(
                    vehicleModelUrl(), HttpMethod.GET, request, VehicleModelDTO[].class);
            if (response.getBody() == null) {
                return List.of();
            }

            // Map VehicleModelDTO to VehicleModel
            return Arrays.stream(response.getBody())
                    .map(dto -> {
                        VehicleType vehicleType = new VehicleType(
                                dto.getVehicleType().getId(),
                                dto.getVehicleType().getName()
                        );
                        return new VehicleModel(dto.getId(), vehicleType, dto.getModelName());
                    })
                    .collect(Collectors.toList());
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

            // Convert VehicleModel to VehicleModelDTO
            VehicleModelDTO vehicleModelDTO = new VehicleModelDTO();
            vehicleModelDTO.setModelName(vehicleModel.getModelName());
            VehicleTypeDTO vehicleTypeDTO = new VehicleTypeDTO();
            vehicleTypeDTO.setId(vehicleModel.getVehicleType().getId());
            // Fetch vehicle type name if needed (optional, since backend only needs ID)
            vehicleTypeDTO.setName(vehicleModel.getVehicleType().getName());
            vehicleModelDTO.setVehicleType(vehicleTypeDTO);

            logger.info("Sending add vehicle model request: {}", vehicleModelDTO);
            HttpEntity<VehicleModelDTO> request = new HttpEntity<>(vehicleModelDTO, headers);
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
    public String showEditVehicleModelForm(@PathVariable Integer id, Model model, HttpSession session) {
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

            ResponseEntity<VehicleModelDTO> response = restTemplate.exchange(
                    vehicleModelUrl() + "/" + id, HttpMethod.GET, request, VehicleModelDTO.class);
            VehicleModelDTO dto = response.getBody();
            if (dto == null) {
                throw new RuntimeException("Vehicle model not found");
            }

            VehicleModel vehicleModel = new VehicleModel();
            vehicleModel.setId(dto.getId());
            vehicleModel.setModelName(dto.getModelName());
            VehicleType vehicleType = new VehicleType();
            vehicleType.setId(dto.getVehicleType().getId());
            vehicleType.setName(dto.getVehicleType().getName());
            vehicleModel.setVehicleType(vehicleType);

            model.addAttribute("vehicleModel", vehicleModel);
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
    public String editVehicleModel(@PathVariable Integer id, @ModelAttribute VehicleModel vehicleModel, HttpSession session, Model model) {
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

            // Convert VehicleModel to VehicleModelDTO
            VehicleModelDTO vehicleModelDTO = new VehicleModelDTO();
            vehicleModelDTO.setId(id); // Use the Integer id directly
            vehicleModelDTO.setModelName(vehicleModel.getModelName());
            VehicleTypeDTO vehicleTypeDTO = new VehicleTypeDTO();
            vehicleTypeDTO.setId(vehicleModel.getVehicleType().getId());
            vehicleTypeDTO.setName(vehicleModel.getVehicleType().getName());
            vehicleModelDTO.setVehicleType(vehicleTypeDTO);

            logger.info("Sending edit vehicle model request: {}", vehicleModelDTO);
            HttpEntity<VehicleModelDTO> request = new HttpEntity<>(vehicleModelDTO, headers);
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
    public String deleteVehicleModel(@PathVariable Integer id, HttpSession session, Model model) {
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
    
 // SERVICE PACKAGE MODULE
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

            ResponseEntity<ServicePackageDTO[]> response = restTemplate.exchange(
                    servicePackageUrl(), HttpMethod.GET, request, ServicePackageDTO[].class);
            if (response.getBody() == null) {
                return List.of();
            }

            // Map ServicePackageDTO to ServicePackage
            return Arrays.stream(response.getBody())
                    .map(dto -> {
                        ServicePackage.VehicleType vehicleType = new ServicePackage.VehicleType(
                                dto.getVehicleType().getId(),
                                dto.getVehicleType().getName()
                        );
                        return new ServicePackage(
                                dto.getId(),
                                dto.getPackageName(),
                                dto.getDescription(),
                                dto.getPrice(),
                                vehicleType
                        );
                    })
                    .collect(Collectors.toList());
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error fetching service packages: Status {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("Failed to fetch service packages: " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            logger.error("Error fetching service packages: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to fetch service packages: " + e.getMessage(), e);
        }
    }

    @GetMapping("/service-package")
    public String listServicePackages(Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "Please log in to access this page.");
            return "redirect:/";
        }

        try {
            model.addAttribute("servicePackages", fetchServicePackages(token));
            model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
        } catch (Exception e) {
            model.addAttribute("error", "Failed to fetch service packages: " + e.getMessage());
        }
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
        try {
            model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
            model.addAttribute("servicePackages", fetchServicePackages(token));
        } catch (Exception e) {
            model.addAttribute("error", "Failed to fetch data: " + e.getMessage());
        }
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
            try {
                model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
                model.addAttribute("servicePackages", fetchServicePackages(token));
            } catch (Exception e) {
                model.addAttribute("error", model.asMap().getOrDefault("error", "") + "; Failed to fetch data: " + e.getMessage());
            }
            return "service-package";
        }

        if (servicePackage.getVehicleType() == null || servicePackage.getVehicleType().getId() == null) {
            model.addAttribute("error", "Please select a valid vehicle type.");
            model.addAttribute("formMode", "add");
            try {
                model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
                model.addAttribute("servicePackages", fetchServicePackages(token));
            } catch (Exception e) {
                model.addAttribute("error", model.asMap().getOrDefault("error", "") + "; Failed to fetch data: " + e.getMessage());
            }
            return "service-package";
        }

        if (servicePackage.getPrice() == null || servicePackage.getPrice() < 0) {
            model.addAttribute("error", "Price must be a non-negative number.");
            model.addAttribute("formMode", "add");
            try {
                model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
                model.addAttribute("servicePackages", fetchServicePackages(token));
            } catch (Exception e) {
                model.addAttribute("error", model.asMap().getOrDefault("error", "") + "; Failed to fetch data: " + e.getMessage());
            }
            return "service-package";
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

            // Convert ServicePackage to ServicePackageDTO
            ServicePackageDTO servicePackageDTO = new ServicePackageDTO();
            servicePackageDTO.setPackageName(servicePackage.getPackageName());
            servicePackageDTO.setDescription(servicePackage.getDescription());
            servicePackageDTO.setPrice(servicePackage.getPrice());
            VehicleTypeDTO vehicleTypeDTO = new VehicleTypeDTO();
            vehicleTypeDTO.setId(servicePackage.getVehicleType().getId());
            vehicleTypeDTO.setName(servicePackage.getVehicleType().getName());
            servicePackageDTO.setVehicleType(vehicleTypeDTO);

            logger.info("Sending add service package request: {}", servicePackageDTO);
            HttpEntity<ServicePackageDTO> request = new HttpEntity<>(servicePackageDTO, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(servicePackageUrl(), request, String.class);
            logger.info("Add service package response: Status {}, Body: {}", response.getStatusCode(), response.getBody());

            return "redirect:/service-package";
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error adding service package: Status {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            model.addAttribute("error", "Failed to create service package: " + e.getResponseBodyAsString());
            model.addAttribute("formMode", "add");
            try {
                model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
                model.addAttribute("servicePackages", fetchServicePackages(token));
            } catch (Exception ex) {
                model.addAttribute("error", model.asMap().getOrDefault("error", "") + "; Failed to fetch data: " + ex.getMessage());
            }
            return "service-package";
        } catch (Exception e) {
            logger.error("Error adding service package: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to create service package: " + e.getMessage());
            model.addAttribute("formMode", "add");
            try {
                model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
                model.addAttribute("servicePackages", fetchServicePackages(token));
            } catch (Exception ex) {
                model.addAttribute("error", model.asMap().getOrDefault("error", "") + "; Failed to fetch data: " + ex.getMessage());
            }
            return "service-package";
        }
    }

    @GetMapping("/service-package/edit/{id}")
    public String showEditServicePackageForm(@PathVariable Integer id, Model model, HttpSession session) {
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

            ResponseEntity<ServicePackageDTO> response = restTemplate.exchange(
                    servicePackageUrl() + "/" + id, HttpMethod.GET, request, ServicePackageDTO.class);
            ServicePackageDTO dto = response.getBody();
            if (dto == null) {
                throw new RuntimeException("Service package not found");
            }

            ServicePackage servicePackage = new ServicePackage();
            servicePackage.setId(dto.getId());
            servicePackage.setPackageName(dto.getPackageName());
            servicePackage.setDescription(dto.getDescription());
            servicePackage.setPrice(dto.getPrice());
            ServicePackage.VehicleType vehicleType = new ServicePackage.VehicleType();
            vehicleType.setId(dto.getVehicleType().getId());
            vehicleType.setName(dto.getVehicleType().getName());
            servicePackage.setVehicleType(vehicleType);

            model.addAttribute("servicePackage", servicePackage);
            model.addAttribute("formMode", "edit");
            model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
            model.addAttribute("servicePackages", fetchServicePackages(token));
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error fetching service package for edit: Status {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            model.addAttribute("error", "Failed to fetch service package: " + e.getResponseBodyAsString());
            model.addAttribute("formMode", "none");
            try {
                model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
                model.addAttribute("servicePackages", fetchServicePackages(token));
            } catch (Exception ex) {
                model.addAttribute("error", model.asMap().getOrDefault("error", "") + "; Failed to fetch data: " + ex.getMessage());
            }
        } catch (Exception e) {
            logger.error("Error fetching service package for edit: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to fetch service package: " + e.getMessage());
            model.addAttribute("formMode", "none");
            try {
                model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
                model.addAttribute("servicePackages", fetchServicePackages(token));
            } catch (Exception ex) {
                model.addAttribute("error", model.asMap().getOrDefault("error", "") + "; Failed to fetch data: " + ex.getMessage());
            }
        }

        return "service-package";
    }

    @PostMapping("/service-package/edit/{id}")
    public String editServicePackage(@PathVariable Integer id, @ModelAttribute ServicePackage servicePackage, HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "Please log in to access this page.");
            return "redirect:/";
        }

        if (servicePackage.getPackageName() == null || servicePackage.getPackageName().trim().isEmpty()) {
            model.addAttribute("error", "Package name is required and cannot be empty.");
            model.addAttribute("formMode", "edit");
            try {
                model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
                model.addAttribute("servicePackages", fetchServicePackages(token));
            } catch (Exception e) {
                model.addAttribute("error", model.asMap().getOrDefault("error", "") + "; Failed to fetch data: " + e.getMessage());
            }
            return "service-package";
        }

        if (servicePackage.getVehicleType() == null || servicePackage.getVehicleType().getId() == null) {
            model.addAttribute("error", "Please select a valid vehicle type.");
            model.addAttribute("formMode", "edit");
            try {
                model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
                model.addAttribute("servicePackages", fetchServicePackages(token));
            } catch (Exception e) {
                model.addAttribute("error", model.asMap().getOrDefault("error", "") + "; Failed to fetch data: " + e.getMessage());
            }
            return "service-package";
        }

        if (servicePackage.getPrice() == null || servicePackage.getPrice() < 0) {
            model.addAttribute("error", "Price must be a non-negative number.");
            model.addAttribute("formMode", "edit");
            try {
                model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
                model.addAttribute("servicePackages", fetchServicePackages(token));
            } catch (Exception e) {
                model.addAttribute("error", model.asMap().getOrDefault("error", "") + "; Failed to fetch data: " + e.getMessage());
            }
            return "service-package";
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

            // Convert ServicePackage to ServicePackageDTO
            ServicePackageDTO servicePackageDTO = new ServicePackageDTO();
            servicePackageDTO.setId(id);
            servicePackageDTO.setPackageName(servicePackage.getPackageName());
            servicePackageDTO.setDescription(servicePackage.getDescription());
            servicePackageDTO.setPrice(servicePackage.getPrice());
            VehicleTypeDTO vehicleTypeDTO = new VehicleTypeDTO();
            vehicleTypeDTO.setId(servicePackage.getVehicleType().getId());
            vehicleTypeDTO.setName(servicePackage.getVehicleType().getName());
            servicePackageDTO.setVehicleType(vehicleTypeDTO);

            logger.info("Sending edit service package request: {}", servicePackageDTO);
            HttpEntity<ServicePackageDTO> request = new HttpEntity<>(servicePackageDTO, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    servicePackageUrl() + "/" + id, HttpMethod.PUT, request, String.class);
            logger.info("Edit service package response: Status {}, Body: {}", response.getStatusCode(), response.getBody());

            return "redirect:/service-package";
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error editing service package: Status {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            model.addAttribute("error", "Failed to update service package: " + e.getResponseBodyAsString());
            model.addAttribute("formMode", "edit");
            try {
                model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
                model.addAttribute("servicePackages", fetchServicePackages(token));
            } catch (Exception ex) {
                model.addAttribute("error", model.asMap().getOrDefault("error", "") + "; Failed to fetch data: " + ex.getMessage());
            }
            return "service-package";
        } catch (Exception e) {
            logger.error("Error editing service package: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to update service package: " + e.getMessage());
            model.addAttribute("formMode", "edit");
            try {
                model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
                model.addAttribute("servicePackages", fetchServicePackages(token));
            } catch (Exception ex) {
                model.addAttribute("error", model.asMap().getOrDefault("error", "") + "; Failed to fetch data: " + ex.getMessage());
            }
            return "service-package";
        }
    }

    @PostMapping("/service-package/delete/{id}")
    public String deleteServicePackage(@PathVariable Integer id, HttpSession session, Model model) {
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
            try {
                model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
                model.addAttribute("servicePackages", fetchServicePackages(token));
            } catch (Exception ex) {
                model.addAttribute("error", model.asMap().getOrDefault("error", "") + "; Failed to fetch data: " + ex.getMessage());
            }
            return "service-package";
        } catch (Exception e) {
            logger.error("Error deleting service package: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to delete service package: " + e.getMessage());
            try {
                model.addAttribute("vehicleTypes", fetchVehicleTypes(token));
                model.addAttribute("servicePackages", fetchServicePackages(token));
            } catch (Exception ex) {
                model.addAttribute("error", model.asMap().getOrDefault("error", "") + "; Failed to fetch data: " + ex.getMessage());
            }
            return "service-package";
        }
    }

    // CUSTOMER MODULE
    private String customerUrl() {
        return backendApiUrl + "/customers";
    }

    private List<CustomerManagement> fetchCustomersList(String token, boolean active) {
        String url = active ? customerUrl() + "/active" : customerUrl() + "/inactive";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<CustomerManagement[]> response = restTemplate.exchange(url, HttpMethod.GET, request, CustomerManagement[].class);
            return response.getBody() != null ? Arrays.asList(response.getBody()) : List.of();
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error fetching customers (active={}): Status {}, Response: {}", active, e.getStatusCode(), e.getResponseBodyAsString());
            return List.of();
        } catch (Exception e) {
            logger.error("Error fetching customers (active={}): {}", active, e.getMessage(), e);
            return List.of();
        }
    }

    private boolean validateCustomer(CustomerManagement customer, Model model, String formMode, String token) {
        if (customer.getUsername() == null || customer.getUsername().trim().isEmpty()) {
            model.addAttribute("error", "Username cannot be empty.");
            setCommonModelAttributes(model, formMode, token, customer);
            return false;
        }
        if (customer.getEmail() == null || customer.getEmail().trim().isEmpty()) {
            model.addAttribute("error", "Email cannot be empty.");
            setCommonModelAttributes(model, formMode, token, customer);
            return false;
        }
        if (customer.getPhoneNumber() == null || customer.getPhoneNumber().trim().isEmpty()) {
            model.addAttribute("error", "Phone number cannot be empty.");
            setCommonModelAttributes(model, formMode, token, customer);
            return false;
        }
        if (formMode.equals("add") && (customer.getPassword() == null || customer.getPassword().trim().isEmpty())) {
            model.addAttribute("error", "Password cannot be empty.");
            setCommonModelAttributes(model, formMode, token, customer);
            return false;
        }
        return true;
    }

    private void setCommonModelAttributes(Model model, String formMode, String token, CustomerManagement customer) {
        model.addAttribute("formMode", formMode);
        model.addAttribute("token", token);
        model.addAttribute("backendApiUrl", backendApiUrl);
        model.addAttribute("profileName", "Admin");
        model.addAttribute("customer", customer);
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
        model.addAttribute("customer", new CustomerManagement());
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

        model.addAttribute("customer", new CustomerManagement());
        model.addAttribute("formMode", "add");
        model.addAttribute("token", token);
        model.addAttribute("backendApiUrl", backendApiUrl);
        model.addAttribute("profileName", "Admin");
        return "customers";
    }

    @PostMapping("/customers/add")
    public String addCustomer(@ModelAttribute("customer") CustomerManagement customer, HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "Please log in to access this page.");
            return "redirect:/";
        }

        if (!validateCustomer(customer, model, "add", token)) {
            return "customers";
        }

        try {
            customer.setActive(true);
            String url = customerUrl();
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

            logger.info("Sending add customer request: {}", customer);
            HttpEntity<CustomerManagement> request = new HttpEntity<>(customer, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            logger.info("Add customer response: Status {}, Body: {}", response.getStatusCode(), response.getBody());

            return "redirect:/customers";
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error adding customer: Status {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            model.addAttribute("error", "Failed to create customer: " + e.getResponseBodyAsString());
            setCommonModelAttributes(model, "add", token, customer);
            return "customers";
        } catch (Exception e) {
            logger.error("Error adding customer: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to create customer: " + e.getMessage());
            setCommonModelAttributes(model, "add", token, customer);
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

            ResponseEntity<CustomerManagement> response = restTemplate.exchange(url, HttpMethod.GET, request, CustomerManagement.class);
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
    public String editCustomer(@PathVariable Long id, @ModelAttribute("customer") CustomerManagement customer, HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "Please log in to access this page.");
            return "redirect:/";
        }

        if (!validateCustomer(customer, model, "edit", token)) {
            return "customers";
        }

        try {
            String url = customerUrl() + "/" + id;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));

            logger.info("Sending edit customer request: {}", customer);
            HttpEntity<CustomerManagement> request = new HttpEntity<>(customer, headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.PUT, request, String.class);
            logger.info("Edit customer response: Status {}, Body: {}", response.getStatusCode(), response.getBody());

            return "redirect:/customers";
        } catch (HttpClientErrorException e) {
            logger.error("HTTP error editing customer: Status {}, Response: {}", e.getStatusCode(), e.getResponseBodyAsString());
            model.addAttribute("error", "Failed to update customer: " + e.getResponseBodyAsString());
            setCommonModelAttributes(model, "edit", token, customer);
            return "customers";
        } catch (Exception e) {
            logger.error("Error editing customer: {}", e.getMessage(), e);
            model.addAttribute("error", "Failed to update customer: " + e.getMessage());
            setCommonModelAttributes(model, "edit", token, customer);
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
    
    // LOGOUT
    @GetMapping("/admin/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

}