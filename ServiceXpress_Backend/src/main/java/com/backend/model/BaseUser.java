package com.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name = "base_user") // Matches your SQL table name
@Data
public class BaseUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    private String password;

   
    @Column(name = "created_at")
    private Date createdAt;
    
    
}