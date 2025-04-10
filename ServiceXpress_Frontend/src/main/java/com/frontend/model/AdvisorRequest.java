package com.frontend.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
public class AdvisorRequest {
	private Long id;
    private String advisorName;
    private String requestedService;
    private String customerName;
    private String vehicleType;
    private String vehicleModel;
    private String serviceCenter;
    private String status;
}
