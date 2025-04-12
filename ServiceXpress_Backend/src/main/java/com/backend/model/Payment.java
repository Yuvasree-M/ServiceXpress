package com.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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
}