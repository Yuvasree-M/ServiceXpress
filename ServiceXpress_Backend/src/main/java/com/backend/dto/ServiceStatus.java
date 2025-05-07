package com.backend.dto;

public class ServiceStatus {
    private Long id;
    private String serviceCenterName;
    private String vehicleTypeName;
    private String vehicleModelName;
    private String registration;
    private String service;
    private Double cost;
    private String status;

    public ServiceStatus() {}

    public ServiceStatus(Long id, String serviceCenterName, String vehicleTypeName, String vehicleModelName,
                         String registration, String service, Double cost, String status) {
        this.id = id;
        this.serviceCenterName = serviceCenterName;
        this.vehicleTypeName = vehicleTypeName;
        this.vehicleModelName = vehicleModelName;
        this.registration = registration;
        this.service = service;
        this.cost = cost;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getServiceCenterName() { return serviceCenterName; }
    public void setServiceCenterName(String serviceCenterName) { this.serviceCenterName = serviceCenterName; }
    public String getVehicleTypeName() { return vehicleTypeName; }
    public void setVehicleTypeName(String vehicleTypeName) { this.vehicleTypeName = vehicleTypeName; }
    public String getVehicleModelName() { return vehicleModelName; }
    public void setVehicleModelName(String vehicleModelName) { this.vehicleModelName = vehicleModelName; }
    public String getRegistration() { return registration; }
    public void setRegistration(String registration) { this.registration = registration; }
    public String getService() { return service; }
    public void setService(String service) { this.service = service; }
    public Double getCost() { return cost; }
    public void setCost(Double cost) { this.cost = cost; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}