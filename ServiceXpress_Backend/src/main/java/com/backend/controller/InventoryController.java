package com.backend.controller;

import com.backend.model.Inventory;
import com.backend.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService service;

    public InventoryController(InventoryService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Inventory> createInventory(@RequestBody Inventory inventory) {
        return ResponseEntity.ok(service.createInventory(inventory));
    }

    @GetMapping
    public ResponseEntity<List<Inventory>> getAllInventory() {
        return ResponseEntity.ok(service.getAllInventory());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Inventory> updateInventory(@PathVariable Long id, @RequestBody Inventory inventory) {
        return ResponseEntity.ok(service.updateInventory(id, inventory));
    }
}