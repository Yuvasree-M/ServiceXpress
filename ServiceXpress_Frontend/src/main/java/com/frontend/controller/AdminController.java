package com.frontend.controller;

import com.frontend.model.DashboardData;
import com.frontend.model.ServiceItem;
import com.frontend.model.VehicleDue;
import com.frontend.model.Advisor;
import com.frontend.model.VehicleCompleted;
import com.frontend.model.VehicleUnderService;
import com.frontend.model.AdvisorRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
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
        
        String url = backendApiUrl + "/dashboard/admin";
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<String> request = new HttpEntity<>(headers);
        DashboardData data = restTemplate.exchange(url, HttpMethod.GET, request, DashboardData.class).getBody();
        model.addAttribute("dashboardData", data);
        model.addAttribute("token", token); 
        
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
    
    @GetMapping("/inventory")
    public String showInventory(Model model, HttpSession session) {
        String token = (String) session.getAttribute("token");
        if (token == null) {
            model.addAttribute("error", "No authentication token found. Please log in.");
            return "index";
        }

        try {
            String url = backendApiUrl + "/inventory";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<ServiceItem[]> response = restTemplate.exchange(url, HttpMethod.GET, request, ServiceItem[].class);

            List<ServiceItem> inventoryList = Arrays.asList(response.getBody());
            model.addAttribute("inventoryList", inventoryList);
            model.addAttribute("profileName", "Admin"); // You can customize

        } catch (Exception e) {
            model.addAttribute("error", "Unable to fetch inventory: " + e.getMessage());
        }

        return "inventory";
    }
    @GetMapping("/inventory/add")
    public String showAddInventoryForm(Model model) {
        model.addAttribute("item", new ServiceItem()); // for form binding
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
            // ✅ Set the lastUpdated field to today
            item.setLastUpdated(java.time.LocalDate.now());

            String url = backendApiUrl + "/inventory";
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            headers.set("Content-Type", "application/json");

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
        if (token == null) {
            model.addAttribute("error", "No authentication token found. Please log in.");
            return "index";
        }

        try {
            // Fetch the inventory item to edit
            String url = backendApiUrl + "/inventory/" + id;
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<String> request = new HttpEntity<>(headers);
            ResponseEntity<ServiceItem> response = restTemplate.exchange(url, HttpMethod.GET, request, ServiceItem.class);
            
            ServiceItem item = response.getBody();
            model.addAttribute("item", item);
            model.addAttribute("token", token);

        } catch (Exception e) {
            model.addAttribute("error", "Unable to fetch item details: " + e.getMessage());
        }

        return "edit-inventory";
    }

    // Handle the form submission to update the inventory item
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
            headers.set("Content-Type", "application/json");

            HttpEntity<ServiceItem> request = new HttpEntity<>(item, headers);
            restTemplate.exchange(url, HttpMethod.PUT, request, String.class);

            return "redirect:/inventory";  // Redirect to inventory list after update

        } catch (Exception e) {
            model.addAttribute("error", "Error updating inventory: " + e.getMessage());
            return "edit-inventory";
        }
    }
    @GetMapping("/admin/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}




