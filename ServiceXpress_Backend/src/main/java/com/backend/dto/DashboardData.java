package com.backend.dto;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardData {
    @JsonProperty("vehiclesDueCount")
    private int vehiclesDueCount;

    @JsonProperty("vehiclesUnderServiceCount")
    private int vehiclesUnderServiceCount;

    @JsonProperty("vehiclesCompletedCount")
    private int vehiclesCompletedCount;

    @JsonProperty("profileName")
    private String profileName;

    @JsonProperty("vehiclesDue")
    private List<VehicleDue> vehiclesDue;

    @JsonProperty("vehiclesUnderService")
    private List<VehicleUnderService> vehiclesUnderService;

    @JsonProperty("vehiclesCompleted")
    private List<VehicleCompleted> vehiclesCompleted;

    @JsonProperty("availableAdvisors")
    private List<AdvisorDTO> availableAdvisors;
}