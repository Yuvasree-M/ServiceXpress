package com.backend.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;

@Entity
@Table(name = "vehicle_types")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class VehicleType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "vehicle_type_id")
    private Long id;

    @Column(name = "type_name", nullable = false)
    private String typeName;

    @JsonIgnore // Ignore vehicleModels in JSON response
    @OneToMany(mappedBy = "vehicleType", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VehicleModel> vehicleModels;
}