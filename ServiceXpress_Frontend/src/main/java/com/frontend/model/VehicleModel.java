package com.frontend.model;

public class VehicleModel {
    private Integer id; // Changed from vehicleModelId to match VehicleModelDTO
    private VehicleType vehicleType;
    private String modelName;

    public VehicleModel() {
        // Default constructor
    }

    public VehicleModel(Integer id, VehicleType vehicleType, String modelName) {
        this.id = id;
        this.vehicleType = vehicleType;
        this.modelName = modelName;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
}