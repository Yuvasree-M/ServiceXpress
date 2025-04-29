package com.frontend.model;

import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceItem {
    private Long id;
    private String workitems;
    private int quantity;
    private double prices;
    private LocalDate lastUpdated;
}