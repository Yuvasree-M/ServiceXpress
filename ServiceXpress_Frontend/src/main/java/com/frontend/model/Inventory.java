package com.frontend.model;

import java.time.LocalDate;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Inventory {
    private Long id;
    private String workitems;
    private int quantity;
    private double prices;
    private LocalDate lastUpdated;
}
