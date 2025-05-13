package com.backend.dto;

import com.backend.model.BookingRequest;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BookingResponseDTO {
    private Long id;
    private Long customerId;
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
    private String vehicleRegistrationNumber;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer vehicleTypeId;
    private Integer vehicleModelId;
    private Long serviceCenterId;
    private String serviceCenter;
    private String vehicleType;
    private String vehicleModel;

    public BookingResponseDTO(BookingRequest booking, String serviceCenter, String vehicleType, String vehicleModel) {
        this.id = booking.getId();
        this.customerId = booking.getCustomerId();
        this.customerName = booking.getCustomerName();
        this.customerPhone = booking.getCustomerPhone();
        this.customerEmail = booking.getCustomerEmail();
        this.address = booking.getAddress();
        this.services = booking.getServices();
        this.pickDropOption = booking.getPickDropOption();
        this.pickupAddress = booking.getPickupAddress();
        this.dropAddress = booking.getDropAddress();
        this.pickupDropoffOption = booking.getPickupDropoffOption();
        this.requestedDate = booking.getRequestedDate();
        this.vehicleRegistrationNumber = booking.getVehicleRegistrationNumber();
        this.status = booking.getStatus();
        this.createdAt = booking.getCreatedAt();
        this.updatedAt = booking.getUpdatedAt();
        this.vehicleTypeId = booking.getVehicleTypeId();
        this.vehicleModelId = booking.getVehicleModelId();
        this.serviceCenterId = booking.getServiceCenterId();
        this.serviceCenter = serviceCenter;
        this.vehicleType = vehicleType;
        this.vehicleModel = vehicleModel;
    }
}