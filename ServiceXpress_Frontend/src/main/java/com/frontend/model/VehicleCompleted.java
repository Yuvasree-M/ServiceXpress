package com.frontend.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor
public class VehicleCompleted {
    private String vehicleNumber;
    private String ownerName;
    private Date completedDate;
    private String status;
}