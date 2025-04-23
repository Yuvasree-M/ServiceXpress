package com.frontend.model;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class VehicleModel {
    private Long id;
    private String modelName;
    private VehicleType vehicleType;
}