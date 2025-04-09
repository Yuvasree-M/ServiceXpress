package com.frontend.model;

import java.util.Date;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class VehicleDue {
    private String ownerName;
    private String vehicleModel;
    private String vehicleType;
    private String serviceNeeded;
    private String location;
    private Long serviceAdvisorId;
    private List<Advisor> availableAdvisors;
    private Date requestedDate;
    private Date dueDate;
    private String status;
}