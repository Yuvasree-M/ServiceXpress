package com.backend.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "inventory")
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "workitems")
    private String workitems;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "prices")
    private Double prices;
}