package com.frontend.model;

public class VehicleModel {
    private Integer vehicleModelId;
    private VehicleType vehicleType;
    private String modelName;
    
    
	public VehicleModel(Integer vehicleModelId, VehicleType vehicleType, String modelName) {
		super();
		this.vehicleModelId = vehicleModelId;
		this.vehicleType = vehicleType;
		this.modelName = modelName;
	}
	public Integer getVehicleModelId() {
		return vehicleModelId;
	}
	public void setVehicleModelId(Integer vehicleModelId) {
		this.vehicleModelId = vehicleModelId;
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
