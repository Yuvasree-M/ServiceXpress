package com.frontend.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServicePackage {
    private Integer id;
    private String packageName;
    private String description;
    private Double price;
    private VehicleType vehicleType;
}
