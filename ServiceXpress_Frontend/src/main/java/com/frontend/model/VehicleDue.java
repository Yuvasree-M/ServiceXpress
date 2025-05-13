package com.frontend.model;

import java.time.LocalDateTime;
import java.util.List;

public class VehicleDue {
    private Long id;
    private String ownerName;
    private String vehicleModel;
    private String vehicleType;
    private String serviceNeeded;
    private String location;
    private LocalDateTime requestedDate;
    private LocalDateTime dueDate;
    private String status;
    private List<AdvisorDTO> availableAdvisors;
    private Long serviceAdvisorId;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    public String getServiceNeeded() { return serviceNeeded; }
    public void setServiceNeeded(String serviceNeeded) { this.serviceNeeded = serviceNeeded; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public LocalDateTime getRequestedDate() { return requestedDate; }
    public void setRequestedDate(LocalDateTime requestedDate) { this.requestedDate = requestedDate; }
    public LocalDateTime getDueDate() { return dueDate; }
    public void setDueDate(LocalDateTime dueDate) { this.dueDate = dueDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public List<AdvisorDTO> getAvailableAdvisors() { return availableAdvisors; }
    public void setAvailableAdvisors(List<AdvisorDTO> availableAdvisors) { this.availableAdvisors = availableAdvisors; }
    public Long getServiceAdvisorId() { return serviceAdvisorId; }
    public void setServiceAdvisorId(Long serviceAdvisorId) { this.serviceAdvisorId = serviceAdvisorId; }
}