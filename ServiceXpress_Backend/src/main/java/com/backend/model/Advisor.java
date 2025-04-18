package com.backend.model;

import jakarta.persistence.*;
import lombok.*;
import com.backend.enums.Role;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "Advisor")
public class Advisor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "city_name", nullable = false)
    private String cityName;

    @Column(name = "center_name", nullable = false)
    private String centerName;

    @Column(name = "advisor_username", nullable = false, unique = true)
    private String username; // Corresponds to 'advisor_username' column

    @Column(name = "advisor_email", unique = true)
    private String email;

    @Column(name = "advisor_phone_number")
    private String phoneNumber;

    @Column(name = "advisor_password")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.SERVICE_ADVISOR;
}
