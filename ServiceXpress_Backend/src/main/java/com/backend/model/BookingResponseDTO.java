package com.backend.model;

import java.time.LocalDateTime;
import java.util.List;

public class BookingResponseDTO {
    private Long id;
    private Long customerId;
    private String serviceCenterId; // Formatted as "id (centername)"
    private String vehicleTypeId;   // Formatted as "id (name)"
    private String vehicleModelId;  // Formatted as "id (modelname)"
    private String vehicleRegistrationNumber;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String address;
    private String services; // Assuming services is a comma-separated string
    private String pickDropOption;
    private String pickupAddress;
    private String dropAddress;
    private String pickupDropoffOption;
    private LocalDateTime requestedDate;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructor
    public BookingResponseDTO(BookingRequest booking, String serviceCenterFormatted, String vehicleTypeFormatted, String vehicleModelFormatted) {
        this.id = booking.getId();
        this.customerId = booking.getCustomerId();
        this.serviceCenterId = serviceCenterFormatted;
        this.vehicleTypeId = vehicleTypeFormatted;
        this.vehicleModelId = vehicleModelFormatted;
        this.vehicleRegistrationNumber = booking.getVehicleRegistrationNumber();
        this.customerName = booking.getCustomerName();
        this.customerPhone = booking.getCustomerPhone();
        this.customerEmail = booking.getCustomerEmail();
        this.address = booking.getAddress();
        this.services = String.join(", ", booking.getServices()); // Convert List to comma-separated string
        this.pickDropOption = booking.getPickDropOption();
        this.pickupAddress = booking.getPickupAddress();
        this.dropAddress = booking.getDropAddress();
        this.pickupDropoffOption = booking.getPickupDropoffOption();
        this.requestedDate = booking.getRequestedDate();
        this.status = booking.getStatus();
        this.createdAt = booking.getCreatedAt();
        this.updatedAt = booking.getUpdatedAt();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getServiceCenterId() {
        return serviceCenterId;
    }

    public void setServiceCenterId(String serviceCenterId) {
        this.serviceCenterId = serviceCenterId;
    }

    public String getVehicleTypeId() {
        return vehicleTypeId;
    }

    public void setVehicleTypeId(String vehicleTypeId) {
        this.vehicleTypeId = vehicleTypeId;
    }

    public String getVehicleModelId() {
        return vehicleModelId;
    }

    public void setVehicleModelId(String vehicleModelId) {
        this.vehicleModelId = vehicleModelId;
    }

    public String getVehicleRegistrationNumber() {
        return vehicleRegistrationNumber;
    }

    public void setVehicleRegistrationNumber(String vehicleRegistrationNumber) {
        this.vehicleRegistrationNumber = vehicleRegistrationNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getServices() {
        return services;
    }

    public void setServices(String services) {
        this.services = services;
    }

    public String getPickDropOption() {
        return pickDropOption;
    }

    public void setPickDropOption(String pickDropOption) {
        this.pickDropOption = pickDropOption;
    }

    public String getPickupAddress() {
        return pickupAddress;
    }

    public void setPickupAddress(String pickupAddress) {
        this.pickupAddress = pickupAddress;
    }

    public String getDropAddress() {
        return dropAddress;
    }

    public void setDropAddress(String dropAddress) {
        this.dropAddress = dropAddress;
    }

    public String getPickupDropoffOption() {
        return pickupDropoffOption;
    }

    public void setPickupDropoffOption(String pickupDropoffOption) {
        this.pickupDropoffOption = pickupDropoffOption;
    }

    public LocalDateTime getRequestedDate() {
        return requestedDate;
    }

    public void setRequestedDate(LocalDateTime requestedDate) {
        this.requestedDate = requestedDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}