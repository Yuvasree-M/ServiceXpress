package com.frontend.model;

public class ServicePackage {
    private Integer id;
    private String packageName;
    private String description;
    private Double price;
    private VehicleType vehicleType;

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

    public static class VehicleType {
        private Integer id;
        private String name;

        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }

    @Override
    public String toString() {
        return "ServicePackage{id=" + id + ", packageName='" + packageName + "', price=" + price + "}";
    }
}