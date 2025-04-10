package com.frontend.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VehicleUnderService {
	private String ownerName;
    private String vehicleType;
    private String vehicleModel;
    private String serviceCenter;
    private String serviceAdvisor;
    private String serviceAllocated;
    private String status;

}