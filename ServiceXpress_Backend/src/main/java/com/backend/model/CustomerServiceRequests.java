package com.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_service_requests")
public class CustomerServiceRequests {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer requestId;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "advisor_id")
    private ServiceLocation advisor;

    @Column(name = "vehicle_type_id")
    private Integer vehicleTypeId;

    @Column(name = "vehicle_model_id")
    private Integer vehicleModelId;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "customer_phone")
    private String customerPhone;

    @Column(name = "customer_address", columnDefinition = "TEXT")
    private String customerAddress;

    @Column(name = "service_item", columnDefinition = "TEXT")
    private String serviceItem;

    @Column(name = "service_status")
    private String serviceStatus;

    @Column(name = "pick_drop_option")
    private String pickDropOption;

    @Column(name = "pickup_address", columnDefinition = "TEXT")
    private String pickupAddress;

    @Column(name = "drop_address", columnDefinition = "TEXT")
    private String dropAddress;

    @Column(name = "requested_date")
    private LocalDateTime requestedDate;

    @Column(name = "completed_date")
    private LocalDateTime completedDate;

    public Integer getRequestId() { return requestId; }
    public void setRequestId(Integer requestId) { this.requestId = requestId; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    public ServiceLocation getAdvisor() { return advisor; }
    public void setAdvisor(ServiceLocation advisor) { this.advisor = advisor; }
    public Integer getVehicleTypeId() { return vehicleTypeId; }
    public void setVehicleTypeId(Integer vehicleTypeId) { this.vehicleTypeId = vehicleTypeId; }
    public Integer getVehicleModelId() { return vehicleModelId; }
    public void setVehicleModelId(Integer vehicleModelId) { this.vehicleModelId = vehicleModelId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public String getCustomerAddress() { return customerAddress; }
    public void setCustomerAddress(String customerAddress) { this.customerAddress = customerAddress; }
    public String getServiceItem() { return serviceItem; }
    public void setServiceItem(String serviceItem) { this.serviceItem = serviceItem; }
    public String getServiceStatus() { return serviceStatus; }
    public void setServiceStatus(String serviceStatus) { this.serviceStatus = serviceStatus; }
    public String getPickDropOption() { return pickDropOption; }
    public void setPickDropOption(String pickDropOption) { this.pickDropOption = pickDropOption; }
    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }
    public String getDropAddress() { return dropAddress; }
    public void setDropAddress(String dropAddress) { this.dropAddress = dropAddress; }
    public LocalDateTime getRequestedDate() { return requestedDate; }
    public void setRequestedDate(LocalDateTime requestedDate) { this.requestedDate = requestedDate; }
    public LocalDateTime getCompletedDate() { return completedDate; }
    public void setCompletedDate(LocalDateTime completedDate) { this.completedDate = completedDate; }
}