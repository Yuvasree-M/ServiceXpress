package com.backend.dto;

import lombok.Data;

@Data
public class VehicleUnderServiceDTO {
    private Long id;
    private String ownerName;
    private String vehicleType;
    private String vehicleModel;
    private String serviceCenter;
    private String serviceAdvisor;
    private String serviceAllocated;
    private String status;
}

