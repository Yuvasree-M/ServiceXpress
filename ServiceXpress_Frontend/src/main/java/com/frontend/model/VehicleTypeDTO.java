package com.frontend.model;

public class VehicleTypeDTO {
    private Integer id;
    private String name;

    public VehicleTypeDTO() {
    }

    public VehicleTypeDTO(Integer id, String name) {
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