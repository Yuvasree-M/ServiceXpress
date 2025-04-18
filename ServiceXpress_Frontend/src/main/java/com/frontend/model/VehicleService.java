package com.frontend.model;

import lombok.Data;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class VehicleService {
    @JsonProperty("id")
    private Long id; // Unique identifier for the service

    @JsonProperty("name")
    private String name; // Name of the vehicle service (e.g., "Oil Change")

    @JsonProperty("price")
    private Double price; // Price of the service

    @JsonProperty("estimatedDate")
    private String estimatedDate; // Estimated completion date

    @JsonProperty("duration")
    private String duration; // Estimated duration of the service

    @JsonProperty("tasks")
    private List<String> tasks; // List of tasks to be performed
}