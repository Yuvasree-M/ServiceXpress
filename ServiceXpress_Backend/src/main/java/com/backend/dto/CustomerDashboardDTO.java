package com.backend.dto;

import com.backend.model.ServiceHistory;
import com.backend.model.ServiceStatus;
import lombok.Data;

import java.util.List;

@Data
public class CustomerDashboardDTO {
    private List<ServiceStatus> ongoingServices;
    private List<ServiceHistory> serviceHistory;
}