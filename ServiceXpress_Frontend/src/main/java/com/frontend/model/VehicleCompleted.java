package com.frontend.model;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor
public class VehicleCompleted {
	private Long id;
    private String ownerName;
    private String vehicleType;
    private String vehicleModel;
    private String serviceCenter;
    private String serviceAdvisor;
    private String serviceDone;
    private Date completedDate;
    private String status;
    private boolean paymentRequested;
    private boolean paymentReceived;
}