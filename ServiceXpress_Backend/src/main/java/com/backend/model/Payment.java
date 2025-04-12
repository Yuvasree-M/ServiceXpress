package com.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "service_item", columnDefinition = "TEXT")
    private String serviceItem;

    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(name = "payment_amount")
    private Double paymentAmount;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getServiceItem() { return serviceItem; }
    public void setServiceItem(String serviceItem) { this.serviceItem = serviceItem; }
    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }
    public Double getPaymentAmount() { return paymentAmount; }
    public void setPaymentAmount(Double paymentAmount) { this.paymentAmount = paymentAmount; }
    public LocalDateTime getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDateTime transactionDate) { this.transactionDate = transactionDate; }
}