package com.backend.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VehicleAssignedDTO {
    private Long id;
    private String customerName;
    private String vehicleModel;
    private String vehicleType;
    private String advisorName;
    private LocalDateTime assignedDate;
    private String status;
    private String servicesNeeded;
}