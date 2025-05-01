package com.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDataDTO {
    private int dueCount;
    private int servicingCount;
    private int completedCount;
    private int advisorRequestsCount;
    private String profileName;
    private List<VehicleDueDTO> vehiclesDue;
    private List<VehicleUnderServiceDTO> vehiclesUnderService;
    private List<VehicleCompletedDTO> vehiclesCompleted;
    private List<AdvisorRequestDTO> advisorRequests;
}