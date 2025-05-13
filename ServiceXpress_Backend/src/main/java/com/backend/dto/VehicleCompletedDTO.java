package com.backend.dto;

import java.time.LocalDateTime;

public class VehicleCompletedDTO {
    private Long id;
    private String ownerName;
    private String vehicleType;
    private String vehicleModel;
    private String serviceCenter;
    private String serviceAdvisor;
    private String serviceDone;
    private LocalDateTime completedDate;
    private String status;
    private String customerEmail;
    private boolean paymentRequested;
    private boolean paymentReceived;
    private boolean hasBom; // New field

    // Getters and setters
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
    public String getServiceDone() { return serviceDone; }
    public void setServiceDone(String serviceDone) { this.serviceDone = serviceDone; }
    public LocalDateTime getCompletedDate() { return completedDate; }
    public void setCompletedDate(LocalDateTime completedDate) { this.completedDate = completedDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public boolean isPaymentRequested() { return paymentRequested; }
    public void setPaymentRequested(boolean paymentRequested) { this.paymentRequested = paymentRequested; }
    public boolean isPaymentReceived() { return paymentReceived; }
    public void setPaymentReceived(boolean paymentReceived) { this.paymentReceived = paymentReceived; }
    public boolean isHasBom() { return hasBom; }
    public void setHasBom(boolean hasBom) { this.hasBom = hasBom; }
}