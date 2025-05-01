package com.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDueDTO {
    private Long id;
    private String ownerName;
    private String vehicleModel;
    private String vehicleType;
    private String serviceNeeded;
    private String location;
    private Long serviceAdvisorId;
    private List<AdvisorDTO> availableAdvisors;
    private LocalDateTime requestedDate;
    private LocalDateTime dueDate;
    private String status;
}