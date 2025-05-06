package com.frontend.model;

import lombok.Data;

@Data
public class ServiceHistory {
    private Long id;
    private String date;
    private String serviceCenterName;
    private String vehicleTypeName; // For template concatenation
    private String vehicleModelName; // For template concatenation
    private String workDone;
    private Double cost;
    private String status;
    private String transactionId;
}