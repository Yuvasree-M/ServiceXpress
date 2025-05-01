package com.frontend.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DashboardDataDTO {
    private int dueCount;
    private int servicingCount;
    private int completedCount;
    private int advisorRequestsCount;
    private String profileName;
    private List<VehicleDue> vehiclesDue;
    private List<VehicleUnderService> vehiclesUnderService;
    private List<VehicleCompleted> vehiclesCompleted;
    private List<AdvisorRequest> advisorRequests;

    public DashboardDataDTO(int dueCount, int servicingCount, int completedCount, int advisorRequestsCount,
                        String profileName, List<VehicleDue> vehiclesDue,
                        List<VehicleUnderService> vehiclesUnderService, List<VehicleCompleted> vehiclesCompleted,
                        List<AdvisorRequest> advisorRequests) {
        this.dueCount = dueCount;
        this.servicingCount = servicingCount;
        this.completedCount = completedCount;
        this.advisorRequestsCount = advisorRequestsCount;
        this.profileName = profileName;
        this.vehiclesDue = vehiclesDue != null ? vehiclesDue : new ArrayList<>();
        this.vehiclesUnderService = vehiclesUnderService != null ? vehiclesUnderService : new ArrayList<>();
        this.vehiclesCompleted = vehiclesCompleted != null ? vehiclesCompleted : new ArrayList<>();
        this.advisorRequests = advisorRequests != null ? advisorRequests : new ArrayList<>();
    }
}