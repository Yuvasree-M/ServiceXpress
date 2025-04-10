package com.frontend.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ServiceItem {
    private String itemName;
    private int quantity;
    private double cost;
    private double totalCost;

    public ServiceItem(String itemName, int quantity, double cost) {
        this.itemName = itemName;
        this.quantity = quantity;
        this.cost = cost;
        this.totalCost = quantity * cost;
    }

}
