package com.backend.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class VehicleCompletedDTO {
    private Long id;
    private Long customerId;
    private String ownerName;
    private String vehicleType;
    private String vehicleModel;
    private String serviceCenter;
    private String serviceAdvisor;
    private String serviceDone;
    private LocalDateTime completedDate;
    private String status;
    private String customerEmail;
    private boolean hasBom;
}
