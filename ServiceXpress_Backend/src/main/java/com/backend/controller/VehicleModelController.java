package com.backend.controller;

import com.backend.model.VehicleModel;
import com.backend.service.VehicleModelService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicle-models")
public class VehicleModelController {

    private final VehicleModelService service;

    public VehicleModelController(VehicleModelService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<VehicleModel>> getAllVehicleModels() {
        return ResponseEntity.ok(service.getAllVehicleModels());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleModel> getVehicleModelById(@PathVariable Integer id) {
        return service.getVehicleModelById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}