package com.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "booking_advisor_mappings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookingAdvisorMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long bookingId;

    @Column(nullable = false)
    private Long advisorId;

    public BookingAdvisorMapping(Long bookingId, Long advisorId) {
        this.bookingId = bookingId;
        this.advisorId = advisorId;
    }
}