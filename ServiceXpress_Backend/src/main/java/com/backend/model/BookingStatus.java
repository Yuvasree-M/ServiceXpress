package com.backend.model;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "booking_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "booking_request_id", nullable = false)
    private BookingRequest bookingRequest;

    @Column
    private String serviceAdvisor;

    @Column
    private LocalDate completedDate;

    @Column(columnDefinition = "TEXT")
    private String billOfMaterials;

    @Column
    private Boolean paymentRequested = false;

    @Column
    private Boolean paymentReceived = false;
}