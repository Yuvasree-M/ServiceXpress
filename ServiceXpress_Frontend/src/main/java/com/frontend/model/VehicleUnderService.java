package com.frontend.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VehicleUnderService {
    private String vehicleNumber;
    private String ownerName;
    private String serviceCenter;
    private String status;
}