package com.backend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleModelDTO {
    private Integer id;
    private String modelName;
    private VehicleTypeDTO vehicleType;
}