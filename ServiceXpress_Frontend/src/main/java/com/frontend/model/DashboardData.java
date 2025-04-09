package com.frontend.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DashboardData {
    private int dueCount;
    private int servicingCount;
    private int completedCount;
    private int advisorRequestsCount;
    private String profileName;
    private List<VehicleDue> vehiclesDue;
    private List<VehicleUnderService> vehiclesUnderService;
    private List<VehicleCompleted> vehiclesCompleted;
    private List<AdvisorRequest> advisorRequests;
}