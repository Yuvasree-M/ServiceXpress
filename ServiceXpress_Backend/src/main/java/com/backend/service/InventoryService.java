package com.backend.service;

import com.backend.model.Inventory;
import com.backend.repository.InventoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    public List<Inventory> getAllInventory() {
        return inventoryRepository.findAll().stream()
            .map(inventory -> {
                if (Double.isNaN(inventory.getPrices()) || inventory.getPrices() == 0.0) {
                    inventory.setPrices(0.0);
                }
                return inventory;
            })
            .collect(Collectors.toList());
    }

    public Optional<Inventory> getInventoryById(Long id) {
        return inventoryRepository.findById(id).map(inventory -> {
            if (Double.isNaN(inventory.getPrices()) || inventory.getPrices() == 0.0) {
                inventory.setPrices(0.0);
            }
            return inventory;
        });
    }

    public Inventory saveInventory(Inventory inventory) {
        if (Double.isNaN(inventory.getPrices()) || inventory.getPrices() == 0.0) {
            inventory.setPrices(0.0);
        }
        inventory.setLastUpdated(LocalDate.now());
        return inventoryRepository.save(inventory);
    }

    public void deleteInventory(Long id) {
        inventoryRepository.deleteById(id);
    }

    public Inventory updateInventory(Long id, Inventory inventory) {
        inventory.setId(id);
        if (Double.isNaN(inventory.getPrices()) || inventory.getPrices() == 0.0) {
            inventory.setPrices(0.0);
        }
        inventory.setLastUpdated(LocalDate.now());
        return inventoryRepository.save(inventory);
    }

    public boolean useItem(Long id, int quantityUsed) {
        Optional<Inventory> inventoryOpt = inventoryRepository.findById(id);
        if (inventoryOpt.isPresent()) {
            Inventory inventory = inventoryOpt.get();
            if (inventory.getQuantity() >= quantityUsed) {
                inventory.setQuantity(inventory.getQuantity() - quantityUsed);
                inventory.setLastUpdated(LocalDate.now());
                inventoryRepository.save(inventory);
                return true;
            }
        }
        return false;
    }
}