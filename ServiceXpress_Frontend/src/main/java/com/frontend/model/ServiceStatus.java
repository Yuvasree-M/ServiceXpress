package com.frontend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ServiceStatus {
    private String vehicleType;
    private String model;
    private String registration;
    private String service;
    private double cost;
    private String status;
}