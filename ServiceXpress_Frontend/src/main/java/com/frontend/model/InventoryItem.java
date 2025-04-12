package com.frontend.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class InventoryItem {
    private String name;
    private String category;
    private int quantity;
    private String unit;
    private String location;
    private String lastUpdated;

    public InventoryItem() {}
    public InventoryItem(String name, String category, int quantity, String unit, String location, String lastUpdated) {
        this.name = name;
        this.category = category;
        this.quantity = quantity;
        this.unit = unit;
        this.location = location;
        this.lastUpdated = lastUpdated;
    }

}
