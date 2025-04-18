package com.frontend.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EngineType {
    private String type;
    private String icon;
    private String description;

    public EngineType(String type, String icon, String description) {
        this.type = type;
        this.icon = icon;
        this.description = description;
    }
}