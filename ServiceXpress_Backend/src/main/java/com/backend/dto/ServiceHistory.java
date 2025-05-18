package com.backend.dto;
import lombok.Data;

@Data
public class ServiceHistory {
    private Long id;
    private String date;                // e.g., "2025-05-01T10:00:00"
    private String serviceCenterName;
    private String vehicleTypeName;
    private String vehicleModelName;
    private String workDone;
    private Double cost;
    private String status;
    private String transactionId;
}