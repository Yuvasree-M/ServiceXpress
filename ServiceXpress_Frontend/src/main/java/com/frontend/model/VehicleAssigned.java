package com.frontend.model;

import java.time.LocalDateTime;

public class VehicleAssigned {
    private Long id;
    private String customerName;
    private String vehicleModel;
    private String vehicleType;
    private String servicesNeeded;
    private String advisorName;
    private LocalDateTime assignedDate;
    private String status;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    public String getServicesNeeded() { return servicesNeeded; }
    public void setServicesNeeded(String servicesNeeded) { this.servicesNeeded = servicesNeeded; }
    public String getAdvisorName() { return advisorName; }
    public void setAdvisorName(String advisorName) { this.advisorName = advisorName; }
    public LocalDateTime getAssignedDate() { return assignedDate; }
    public void setAssignedDate(LocalDateTime assignedDate) { this.assignedDate = assignedDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}