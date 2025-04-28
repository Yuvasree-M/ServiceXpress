package com.frontend.controller;

import com.frontend.model.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Controller
public class AdminController {

    @Value("${backend.api.url}")
    private String backendApiUrl;

    private final RestTemplate restTemplate;

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

        } catch (Exception e) {
            model.addAttribute("error", "Failed to load dashboard: " + e.getMessage());
        }

        addMockData(model);
        return "admin-dashboard";
    }

    private void addMockData(Model model) {
        DashboardData dashboardData = new DashboardData(
                1, 1, 1, 1, "Admin User",
                List.of(
                        new VehicleDue("John Doe", "Honda Civic", "Sedan", "Oil Change", "Bangalore", 1L,
                                List.of(new Advisor(1L, "Alice Smith"), new Advisor(2L, "Bob Johnson")),
                                new Date(), new Date(), "Requested")
                ),
                List.of(
                        new VehicleUnderService("Jane Smith", "Sedan", "Honda City", "Center A", "John Doe", "Engine Check", "In Progress")
                ),
                List.of(
                        new VehicleCompleted(1L, "Sam White", "SUV", "Hyundai Creta", "Center A", "John Doe", "Oil Change, Brake Check",
                                new Date(), "Completed", false, false)
                ),
                List.of(
                        new AdvisorRequest(1L, "Mark Advisor", "Wheel Alignment", "Jane Smith", "SUV", "Mahindra XUV500", "Center A", "Pending")
                )
        );

        model.addAttribute("dashboardData", dashboardData);
        model.addAttribute("profileName", dashboardData.getProfileName());
    }

    // INVENTORY MODULE
    @GetMapping("/inventory")
    public String showInventory(Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null) return "redirect:/";

        try {
            String url = backendApiUrl + "/inventory";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<ServiceItem[]> response = restTemplate.exchange(url, HttpMethod.GET, request, ServiceItem[].class);
            List<ServiceItem> inventoryList = Arrays.asList(response.getBody());

            model.addAttribute("inventoryList", inventoryList);
            model.addAttribute("profileName", "Admin");

        } catch (Exception e) {
            model.addAttribute("error", "Unable to fetch inventory: " + e.getMessage());
        }

        return "inventory";
    }

    @GetMapping("/inventory/add")
    public String showAddInventoryForm(Model model) {
        model.addAttribute("item", new ServiceItem());
        return "add-inventory";
    }

    @PostMapping("/inventory/add")
    public String addInventory(@ModelAttribute ServiceItem item, HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");

        if (item.getWorkitems() == null || item.getWorkitems().trim().isEmpty()) {
            model.addAttribute("error", "Item name cannot be empty.");
            return "add-inventory";
        }

        try {
            item.setLastUpdated(LocalDate.now());

            String url = backendApiUrl + "/inventory";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<ServiceItem> request = new HttpEntity<>(item, headers);
            restTemplate.postForEntity(url, request, String.class);

            return "redirect:/inventory";

        } catch (Exception e) {
            model.addAttribute("error", "Error adding inventory: " + e.getMessage());
            return "add-inventory";
        }
    }

    @GetMapping("/inventory/delete/{id}")
    public String deleteInventory(@PathVariable Long id, HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");

        try {
            String url = backendApiUrl + "/inventory/" + id;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);

            HttpEntity<String> request = new HttpEntity<>(headers);
            restTemplate.exchange(url, HttpMethod.DELETE, request, String.class);

        } catch (Exception e) {
            model.addAttribute("error", "Failed to delete inventory item: " + e.getMessage());
        }

        return "redirect:/inventory";
    }

    @GetMapping("/inventory/edit/{id}")
    public String showEditInventory(@PathVariable Long id, Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null) return "redirect:/";

        try {
            String url = backendApiUrl + "/inventory/" + id;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);

            HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<ServiceItem> response = restTemplate.exchange(url, HttpMethod.GET, request, ServiceItem.class);

            model.addAttribute("item", response.getBody());

        } catch (Exception e) {
            model.addAttribute("error", "Unable to fetch item details: " + e.getMessage());
        }

        return "edit-inventory";
    }

    @PostMapping("/inventory/edit/{id}")
    public String editInventory(@PathVariable Long id, @ModelAttribute ServiceItem item, HttpSession session, Model model) {
        String token = (String) session.getAttribute("token");

        if (item.getWorkitems() == null || item.getWorkitems().trim().isEmpty()) {
            model.addAttribute("error", "Item name cannot be empty.");
            return "edit-inventory";
        }

        try {
            String url = backendApiUrl + "/inventory/" + id;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<ServiceItem> request = new HttpEntity<>(item, headers);
            restTemplate.exchange(url, HttpMethod.PUT, request, String.class);

            return "redirect:/inventory";

        } catch (Exception e) {
            model.addAttribute("error", "Error updating inventory: " + e.getMessage());
            return "edit-inventory";
        }
    }

    // LOGOUT
    @GetMapping("/admin/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
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