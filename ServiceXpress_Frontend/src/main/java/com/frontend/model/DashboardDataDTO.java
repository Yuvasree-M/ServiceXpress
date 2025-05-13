package com.frontend.model;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DashboardDataDTO {
    private List<VehicleDue> vehiclesDue;
    private int dueCount;
    private List<VehicleAssigned> vehiclesAssigned;
    private int assignedCount;
    private List<VehicleUnderService> vehiclesUnderService;
    private int servicingCount;
    private List<VehicleCompleted> vehiclesCompleted;
    private int completedCount;
    private List<AdvisorRequest> advisorRequests;
    private int advisorRequestsCount;
}