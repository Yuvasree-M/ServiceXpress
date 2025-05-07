package com.backend.dto;

public class ServiceHistory {
    private Long id;
    private String date;
    private String serviceCenterName;
    private String vehicleTypeName;
    private String vehicleModelName;
    private String workDone;
    private Double cost;
    private String status;
    private String transactionId;

    public ServiceHistory() {}

    public ServiceHistory(Long id, String date, String serviceCenterName, String vehicleTypeName, String vehicleModelName,
                          String workDone, Double cost, String status, String transactionId) {
        this.id = id;
        this.date = date;
        this.serviceCenterName = serviceCenterName;
        this.vehicleTypeName = vehicleTypeName;
        this.vehicleModelName = vehicleModelName;
        this.workDone = workDone;
        this.cost = cost;
        this.status = status;
        this.transactionId = transactionId;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getServiceCenterName() { return serviceCenterName; }
    public void setServiceCenterName(String serviceCenterName) { this.serviceCenterName = serviceCenterName; }
    public String getVehicleTypeName() { return vehicleTypeName; }
    public void setVehicleTypeName(String vehicleTypeName) { this.vehicleTypeName = vehicleTypeName; }
    public String getVehicleModelName() { return vehicleModelName; }
    public void setVehicleModelName(String vehicleModelName) { this.vehicleModelName = vehicleModelName; }
    public String getWorkDone() { return workDone; }
    public void setWorkDone(String workDone) { this.workDone = workDone; }
    public Double getCost() { return cost; }
    public void setCost(Double cost) { this.cost = cost; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
}