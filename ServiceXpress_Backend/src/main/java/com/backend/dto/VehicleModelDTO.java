package com.backend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleModelDTO {
    private Long id;
    private String modelName;
    private VehicleTypeDTO vehicleType;
}