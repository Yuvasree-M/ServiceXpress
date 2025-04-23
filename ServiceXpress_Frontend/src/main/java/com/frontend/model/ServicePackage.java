package com.frontend.model;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServicePackage {
    private Long id;
    private String packageName;
    private String description;
    private Double price;
    private VehicleType vehicleType;
}