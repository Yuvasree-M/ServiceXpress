package com.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "bill_of_material")
public class BillOfMaterial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id")
    private Long bookingId;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "advisor_name")
    private String advisorName;

    @Column(name = "service_name")
    private String serviceName;

    @Column(name = "materials", columnDefinition = "TEXT")
    private String materials;  // Store the list of materials as a JSON string

    private Double total;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBookingId() { return bookingId; }
    public void setBookingId(Long bookingId) { this.bookingId = bookingId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getAdvisorName() { return advisorName; }
    public void setAdvisorName(String advisorName) { this.advisorName = advisorName; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public String getMaterials() { return materials; }
    public void setMaterials(String materials) { this.materials = materials; }
    public Double getTotal() { return total; }
    public void setTotal(Double total) { this.total = total; }
}