package com.backend.model;

import java.time.LocalDate;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Inventory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String workitems;  // Name of the work item
    private int quantity;      // Quantity of the item
    private double prices;     // Price of the item
    private LocalDate lastUpdated;  // The date the item was last updated
}
