package com.frontend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
public class VehicleDue {
    private String customerName;
    private String vehicleModel;
    private String vehicleType;
    private String serviceType;
    private String location;
    private Long id;
    private List<Advisor> advisors; // Expects List<Advisor>
    private Date dueDate;
    private Date createdDate;
    private String status;
}