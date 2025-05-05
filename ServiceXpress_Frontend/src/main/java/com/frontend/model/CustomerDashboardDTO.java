package com.frontend.model;

import lombok.Data;
import java.util.List;

@Data
public class CustomerDashboardDTO {
    private List<ServiceStatus> ongoingServices;
    private List<ServiceHistory> serviceHistory;
}