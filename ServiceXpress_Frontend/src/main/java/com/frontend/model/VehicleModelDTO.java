package com.frontend.model;

public class VehicleModelDTO {
    private Integer id;
    private String modelName;
    private VehicleTypeDTO vehicleType;

    public VehicleModelDTO() {
    }

    public VehicleModelDTO(Integer id, String modelName, VehicleTypeDTO vehicleType) {
        this.id = id;
        this.modelName = modelName;
        this.vehicleType = vehicleType;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public VehicleTypeDTO getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleTypeDTO vehicleType) {
        this.vehicleType = vehicleType;
    }
}