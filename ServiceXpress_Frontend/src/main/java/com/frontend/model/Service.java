package com.frontend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
public class Service {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private String duration;
    private List<String> tasks;
    private String vehicleType;

}