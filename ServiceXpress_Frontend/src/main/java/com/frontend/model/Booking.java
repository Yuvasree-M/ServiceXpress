package com.frontend.model;

import java.time.LocalDateTime;

public class Booking {
    private Long customerId;
    private Long serviceCenterId;
    private Integer vehicleTypeId;
    private Integer vehicleModelId;
    private String vehicleRegistrationNumber; // New field
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String address;
    private String services;
    private String pickDropOption;
    private String pickupAddress;
    private String dropAddress;
    private String pickupDropoffOption;
    private LocalDateTime requestedDate;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public Booking() {}

    // Getters and Setters
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Long getServiceCenterId() { return serviceCenterId; }
    public void setServiceCenterId(Long serviceCenterId) { this.serviceCenterId = serviceCenterId; }
    public Integer getVehicleTypeId() { return vehicleTypeId; }
    public void setVehicleTypeId(Integer vehicleTypeId) { this.vehicleTypeId = vehicleTypeId; }
    public Integer getVehicleModelId() { return vehicleModelId; }
    public void setVehicleModelId(Integer vehicleModelId) { this.vehicleModelId = vehicleModelId; }
    public String getVehicleRegistrationNumber() { return vehicleRegistrationNumber; }
    public void setVehicleRegistrationNumber(String vehicleRegistrationNumber) { this.vehicleRegistrationNumber = vehicleRegistrationNumber; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getServices() { return services; }
    public void setServices(String services) { this.services = services; }
    public String getPickDropOption() { return pickDropOption; }
    public void setPickDropOption(String pickDropOption) { this.pickDropOption = pickDropOption; }
    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }
    public String getDropAddress() { return dropAddress; }
    public void setDropAddress(String dropAddress) { this.dropAddress = dropAddress; }
    public String getPickupDropoffOption() { return pickupDropoffOption; }
    public void setPickupDropoffOption(String pickupDropoffOption) { this.pickupDropoffOption = pickupDropoffOption; }
    public LocalDateTime getRequestedDate() { return requestedDate; }
    public void setRequestedDate(LocalDateTime requestedDate) { this.requestedDate = requestedDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}