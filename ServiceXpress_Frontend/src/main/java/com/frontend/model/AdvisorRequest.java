package com.frontend.model;

public class AdvisorRequest {
    private Long id;
    private String advisorName;
    private String requestedService;
    private String customerName;
    private String vehicleType;
    private String vehicleModel;
    private String serviceCenter;
    private String status;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getAdvisorName() { return advisorName; }
    public void setAdvisorName(String advisorName) { this.advisorName = advisorName; }
    public String getRequestedService() { return requestedService; }
    public void setRequestedService(String requestedService) { this.requestedService = requestedService; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }
    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }
    public String getServiceCenter() { return serviceCenter; }
    public void setServiceCenter(String serviceCenter) { this.serviceCenter = serviceCenter; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}