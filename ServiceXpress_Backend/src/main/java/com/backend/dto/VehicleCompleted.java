package com.backend.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleCompleted {
    private Long id;
    private String customerName;
    private String vehicleType;
    private String vehicleModel;
    private String serviceCenter;
    private String serviceAdvisor;
    private String services;
    private LocalDate completedDate;
    private String status;
    private String billOfMaterials;
    private boolean paymentRequested;
    private boolean paymentReceived;
}