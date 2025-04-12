package com.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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

    @ManyToOne
    @JoinColumn(name = "vehicle_type_id", nullable = false)
    private VehicleType vehicleType;

    @ManyToOne
    @JoinColumn(name = "vehicle_model_id", nullable = false)
    private VehicleModel vehicleModel;

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
}
