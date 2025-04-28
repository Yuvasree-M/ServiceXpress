package com.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDue {
    private Long id;
    private String customerName;
    private String vehicleType;
    private String vehicleModel;
    private String services;
    private String serviceCenter;
    private Long serviceCenterId;
    private LocalDate requestedDate;
    private String status;
}