package com.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bill_of_material")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BillOfMaterial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", nullable = false)
    private Long bookingId;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "advisor_name")
    private String advisorName;

    @Column(name = "service_name")
    private String serviceName;

    @Column(columnDefinition = "TEXT")
    private String materials;

    @Column(name = "service_charges")
    private Double serviceCharges;
    
    @Column
    private Double total;
}