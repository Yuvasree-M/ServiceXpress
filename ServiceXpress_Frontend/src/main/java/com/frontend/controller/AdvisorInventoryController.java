package com.frontend.controller;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import com.frontend.model.Inventory;

@Controller
@RequestMapping("/advisor/inventory")
public class AdvisorInventoryController {

    @Autowired
    private RestTemplate restTemplate;

    private final String backendUrl = "http://localhost:8081/api/inventory";

    @GetMapping
    public String viewInventory(Model model) {
        Inventory[] items = restTemplate.getForObject(backendUrl, Inventory[].class);
        model.addAttribute("inventoryList", Arrays.asList(items));
        return "advisor/inventory"; // view-only table
    }
}
