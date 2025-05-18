package com.frontend.model;

import lombok.Data;

@Data
public class ServiceHistory {
    private Long id;
    private String date;
    private String serviceCenterName;
    private String vehicleTypeName;
    private String vehicleModelName;
    private String workDone;
    private Double cost;
    private String status;
    private String transactionId;
    private String customerName;
}