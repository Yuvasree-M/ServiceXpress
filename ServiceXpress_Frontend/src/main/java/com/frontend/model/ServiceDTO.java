package com.frontend.model;

public class ServiceDTO {

    private Long id;
    private String vehicleId;
    private String customerName;
    private String assignedDate;
    private String requiredServices;
    private String status;

    public ServiceDTO() {
    }

    public ServiceDTO(Long id, String vehicleId, String customerName, String assignedDate, String requiredServices, String status) {
        this.id = id;
        this.vehicleId = vehicleId;
        this.customerName = customerName;
        this.assignedDate = assignedDate;
        this.requiredServices = requiredServices;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getAssignedDate() {
        return assignedDate;
    }

    public void setAssignedDate(String assignedDate) {
        this.assignedDate = assignedDate;
    }

    public String getRequiredServices() {
        return requiredServices;
    }

    public void setRequiredServices(String requiredServices) {
        this.requiredServices = requiredServices;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}