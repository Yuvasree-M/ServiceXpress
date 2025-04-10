package com.frontend.controller;

import com.frontend.model.ServiceItem;
import com.frontend.model.Vehicle;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class ServiceAdvisorController {

    private final List<Vehicle> vehicles = new ArrayList<>();
    private final List<Vehicle> completedVehicles = new ArrayList<>();
    private final List<Vehicle> adminRequests = new ArrayList<>();

    public ServiceAdvisorController() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            vehicles.add(new Vehicle("V00123", "Toyota Camry - ABC-1234", "Jane Smith", sdf.parse("2025-03-26"), "Engine Oil, Wheel Alignment", "Assigned"));
            vehicles.add(new Vehicle("V00124", "Honda Civic - XYZ-5678", "John Doe", sdf.parse("2025-03-27"), "Fuel Filter", "Assigned"));
            vehicles.add(new Vehicle("V00125", "Ford Mustang - DEF-9012", "Alice Johnson", sdf.parse("2025-03-25"), "Brake Pads, Tire Rotation", "Assigned"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/service-advisor/dashboard")
    public String dashboard(Model model, HttpSession session) {
        if (session.getAttribute("token") == null) {
            model.addAttribute("error", "Session expired. Please login again.");
            return "index";
        }

        model.addAttribute("vehicles", vehicles);
        model.addAttribute("completedVehicles", completedVehicles);
        model.addAttribute("adminRequests", adminRequests);
        model.addAttribute("userName", "Boomika Mohan");
        return "service-advisor-dashboard";
    }

    @PostMapping("/service-advisor/startService/{vehicleId}")
    public String startService(@PathVariable String vehicleId) {
        vehicles.stream().filter(v -> v.getId().equals(vehicleId)).findFirst().ifPresent(v -> v.setStatus("In Progress"));
        return "redirect:/service-advisor/dashboard";
    }

    @PostMapping("/service-advisor/addServiceItem/{vehicleId}")
    public String addServiceItem(@PathVariable String vehicleId,
                                 @RequestParam String serviceItem,
                                 @RequestParam int quantity) {
        Vehicle vehicle = vehicles.stream().filter(v -> v.getId().equals(vehicleId)).findFirst().orElse(null);
        if (vehicle != null) {
            String[] parts = serviceItem.split(" - ");
            double cost = Double.parseDouble(parts[1]);
            vehicle.getServiceItems().add(new ServiceItem(parts[0], quantity, cost));
            vehicle.setTotalCost(vehicle.getServiceItems().stream().mapToDouble(ServiceItem::getTotalCost).sum());
        }
        return "redirect:/service-advisor/dashboard";
    }

    @PostMapping("/service-advisor/markComplete/{vehicleId}")
    public String markComplete(@PathVariable String vehicleId) {
        Vehicle vehicle = vehicles.stream().filter(v -> v.getId().equals(vehicleId)).findFirst().orElse(null);
        if (vehicle != null) {
            vehicle.setStatus("Completed");
            vehicle.setCompletionDate(new Date());
            completedVehicles.add(vehicle);
            vehicles.remove(vehicle);
        }
        return "redirect:/service-advisor/dashboard";
    }

    @PostMapping("/service-advisor/raiseToAdmin/{vehicleId}")
    public String raiseToAdmin(@PathVariable String vehicleId,
                               @RequestParam String serviceItem,
                               @RequestParam int quantity) {
        Vehicle vehicle = vehicles.stream().filter(v -> v.getId().equals(vehicleId)).findFirst().orElse(null);
        if (vehicle != null) {
            String[] parts = serviceItem.split(" - ");
            double cost = Double.parseDouble(parts[1]);
            Vehicle request = new Vehicle(vehicleId, vehicle.getDetails(), vehicle.getCustomerName(), new Date(), parts[0], "Requested");
            request.getServiceItems().add(new ServiceItem(parts[0], quantity, cost));
            request.setTotalCost(cost * quantity);
            adminRequests.add(request);
        }
        return "redirect:/service-advisor/dashboard";
    }

    @GetMapping("/service-advisor/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
