package com.frontend.model;

public class VehicleUnderService {
    private Long id;
    private String ownerName;
    private String vehicleType;
    private String vehicleModel;
    private String serviceCenter;
    private String serviceAdvisor;
    private String serviceAllocated;
    private String status;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public String getServiceAllocated() { return serviceAllocated; }
    public void setServiceAllocated(String serviceAllocated) { this.serviceAllocated = serviceAllocated; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}