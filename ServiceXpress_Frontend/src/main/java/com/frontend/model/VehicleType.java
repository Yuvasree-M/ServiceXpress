package com.frontend.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VehicleType {
    private String type;
    private String imageUrl;
    private String description;

    public VehicleType(String type, String imageUrl, String description) {
        this.type = type;
        this.imageUrl = imageUrl;
        this.description = description;
    }
}