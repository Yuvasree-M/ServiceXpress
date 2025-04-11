package com.frontend.controller;

import com.frontend.model.Customer;
import com.frontend.model.ServiceStatus;

import jakarta.servlet.http.HttpSession;

import com.frontend.model.ServiceHistory;
import com.frontend.model.CustomerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @GetMapping("/customer/dashboard")
    public String showDashboard(Model model) {
        Customer customer = customerService.getLoggedInCustomer();
        List<ServiceStatus> ongoingServices = customerService.getOngoingServices(customer.getId());
        List<ServiceHistory> serviceHistory = customerService.getServiceHistory(customer.getId());

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
}
