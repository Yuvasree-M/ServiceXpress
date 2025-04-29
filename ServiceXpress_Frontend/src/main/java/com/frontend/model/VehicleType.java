package com.frontend.model;

public class VehicleType {
    private Integer id;
    private String name; // Changed from typeName to match VehicleTypeDTO

    public VehicleType() {
        // Default constructor
    }

    public VehicleType(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}