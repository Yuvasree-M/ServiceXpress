package com.frontend.model;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ServiceItem {
    private Long id;
    private String workitems;
    private Integer quantity;
    private Double prices;
    private LocalDate lastUpdated;
}
