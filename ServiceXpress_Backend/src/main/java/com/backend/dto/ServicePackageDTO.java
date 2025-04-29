package com.backend.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServicePackageDTO {
    private Integer id;
    private String packageName;
    private String description;
    private Double price;
    private VehicleTypeDTO vehicleType;
}