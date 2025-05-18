package com.backend.dto;

import lombok.Data;

@Data
public class ServiceStatus {
    private Long id;
    private String serviceCenterName;   // e.g., "Mumbai Center 1 (Mumbai)"
    private String vehicleTypeName;     // e.g., "Bike"
    private String vehicleModelName;    // e.g., "Pulsar 150"
    private String registration;
    private String service;
    private Double cost;
    private String status;
}
