package com.frontend.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Vehicle {
    private String id;
    private String details;
    private String customerName;
    private Date assignedDate;
    private String requiredServices;
    private String status;
    private List<ServiceItem> serviceItems = new ArrayList<>();
    private double totalCost;
    private Date completionDate;

    public Vehicle(String id, String details, String customerName, Date assignedDate, String requiredServices, String status) {
        this.id = id;
        this.details = details;
        this.customerName = customerName;
        this.assignedDate = assignedDate;
        this.requiredServices = requiredServices;
        this.status = status;
    }
}
