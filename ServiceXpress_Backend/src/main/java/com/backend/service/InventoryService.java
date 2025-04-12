package com.backend.service;

import com.backend.model.Inventory;
import com.backend.repository.InventoryRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InventoryService {

    private final InventoryRepository repository;

    public InventoryService(InventoryRepository repository) {
        this.repository = repository;
    }

    public Inventory createInventory(Inventory inventory) {
        return repository.save(inventory);
    }

    public List<Inventory> getAllInventory() {
        return repository.findAll();
    }

    public Inventory updateInventory(Long id, Inventory inventory) {
        Inventory existing = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Inventory not found"));
        existing.setWorkitems(inventory.getWorkitems());
        existing.setQuantity(inventory.getQuantity());
        existing.setPrices(inventory.getPrices());
        return repository.save(existing);
    }
}