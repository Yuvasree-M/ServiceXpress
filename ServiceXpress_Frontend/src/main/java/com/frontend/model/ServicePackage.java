package com.frontend.model;

public class ServicePackage {
    private Integer id;
    private String packageName;
    private String description;
    private Double price;
    private VehicleType vehicleType;

    public ServicePackage() {
    }

    public ServicePackage(Integer id, String packageName, String description, Double price, VehicleType vehicleType) {
        this.id = id;
        this.packageName = packageName;
        this.description = description;
        this.price = price;
        this.vehicleType = vehicleType;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public VehicleType getVehicleType() { return vehicleType; }
    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }
}