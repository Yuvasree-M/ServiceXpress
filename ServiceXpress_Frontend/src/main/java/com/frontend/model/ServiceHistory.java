package com.frontend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor 
public class ServiceHistory {
    private String date;
    private String vehicle;
    private String workDone;
    private double cost;
    private String status;
    private String transactionId;
    // Getters and setters
}
