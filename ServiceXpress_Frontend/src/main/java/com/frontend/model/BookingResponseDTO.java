package com.frontend.model;

import java.time.LocalDateTime;

public class BookingResponseDTO {
    private Long id;
    private Long customerId;
    private String serviceCenterId; // Formatted as "id (centername)"
    private String vehicleTypeId;   // Formatted as "id (name)"
    private String vehicleModelId;  // Formatted as "id (modelname)"
    private String vehicleModel;    // Added to match backend
    private String vehicleRegistrationNumber;
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

    // Default Constructor
    public BookingResponseDTO() {
    }

    // Parameterized Constructor
    public BookingResponseDTO(Long id, Long customerId, String serviceCenterId, String vehicleTypeId,
                             String vehicleModelId, String vehicleModel, String vehicleRegistrationNumber,
                             String customerName, String customerPhone, String customerEmail, String address,
                             String services, String pickDropOption, String pickupAddress, String dropAddress,
                             String pickupDropoffOption, LocalDateTime requestedDate, String status,
                             LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.customerId = customerId;
        this.serviceCenterId = serviceCenterId;
        this.vehicleTypeId = vehicleTypeId;
        this.vehicleModelId = vehicleModelId;
        this.vehicleModel = vehicleModel;
        this.vehicleRegistrationNumber = vehicleRegistrationNumber;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerEmail = customerEmail;
        this.address = address;
        this.services = services;
        this.pickDropOption = pickDropOption;
        this.pickupAddress = pickupAddress;
        this.dropAddress = dropAddress;
        this.pickupDropoffOption = pickupDropoffOption;
        this.requestedDate = requestedDate;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getServiceCenterId() { return serviceCenterId; }
    public void setServiceCenterId(String serviceCenterId) { this.serviceCenterId = serviceCenterId; }
    public String getVehicleTypeId() { return vehicleTypeId; }
    public void setVehicleTypeId(String vehicleTypeId) { this.vehicleTypeId = vehicleTypeId; }
    public String getVehicleModelId() { return vehicleModelId; }
    public void setVehicleModelId(String vehicleModelId) { this.vehicleModelId = vehicleModelId; }
    public String getVehicleModel() { return vehicleModel; }
    public void setVehicleModel(String vehicleModel) { this.vehicleModel = vehicleModel; }
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