
package com.backend.dto;

import lombok.Data;

@Data
public class AdvisorRequestDTO {
    private Long id;
    private String advisorName;
    private String requestedService;
    private String customerName;
    private String vehicleType;
    private String vehicleModel;
    private String serviceCenter;
    private String status;
}