package com.frontend.model;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class ServiceStatus {
    private Long id;
    private String serviceCenterName;
    @JsonProperty("vehicleTypeName")
    private String vehicleType; // Maps to vehicleTypeName in JSON
    @JsonProperty("vehicleModelName")
    private String model; // Maps to vehicleModelName in JSON
    private String registration;
    private String service;
    private Double cost;
    private String status;
}