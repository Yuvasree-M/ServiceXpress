package com.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleUnderService {
    private Long id;
    private String customerName;
    private String vehicleType;
    private String vehicleModel;
    private String serviceCenter;
    private String serviceAdvisor;
    private String services;
    private String status;
}