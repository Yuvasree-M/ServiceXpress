package com.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "booking_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private Long serviceCenterId;

    @Column(nullable = false)
    private Long vehicleTypeId;

    @Column(nullable = false)
    private Long vehicleModelId;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String customerPhone;

    private String customerEmail;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String services;

    @Column(nullable = false)
    private String pickDropOption;

    @Column(columnDefinition = "TEXT")
    private String pickupAddress;

    @Column(columnDefinition = "TEXT")
    private String dropAddress;

    private String pickupDropoffOption;

    @Column(nullable = false)
    private LocalDate requestedDate;

    @Column(nullable = false)
    private String status = "PENDING";

    @Column(nullable = false)
    private LocalDate updatedAt;
}