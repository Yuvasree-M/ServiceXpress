package com.backend.dto;

import com.backend.dto.ServiceHistory;
import com.backend.dto.ServiceStatus;
import lombok.Data;

import java.util.List;

@Data
public class CustomerDashboardDTO {
    private List<ServiceStatus> ongoingServices;
    private List<ServiceHistory> serviceHistory;
}