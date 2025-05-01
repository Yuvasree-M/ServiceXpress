package com.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "booking_advisor_mappings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingAdvisorMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long bookingId;

    @Column(nullable = false)
    private Long advisorId;
}