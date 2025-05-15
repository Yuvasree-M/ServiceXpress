package com.frontend.model;

import java.time.LocalDateTime;

public class VehicleCompleted {
    private Long id;
    private Long customerId; // Added
    private String ownerName;
    private String vehicleType;
    private String vehicleModel;
    private String serviceCenter;
    private String serviceAdvisor;
    private String serviceDone;
    private LocalDateTime completedDate;
    private String status;
    private String customerEmail;
    private boolean hasBom;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }

    public String getServiceCenter() { return serviceCenter; }
    public void setServiceCenter(String serviceCenter) { this.serviceCenter = serviceCenter; }

    public String getServiceAdvisor() { return serviceAdvisor; }
    public void setServiceAdvisor(String serviceAdvisor) { this.serviceAdvisor = serviceAdvisor; }

    public String getServiceDone() { return serviceDone; }
    public void setServiceDone(String serviceDone) { this.serviceDone = serviceDone; }

    public LocalDateTime getCompletedDate() { return completedDate; }
    public void setCompletedDate(LocalDateTime completedDate) { this.completedDate = completedDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public boolean isHasBom() { return hasBom; }
    public void setHasBom(boolean hasBom) { this.hasBom = hasBom; }
}