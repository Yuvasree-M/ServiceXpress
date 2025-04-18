package com.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.model.VehicleType;
import com.backend.service.VehicleTypeService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/vehicle-types")
@RequiredArgsConstructor
public class VehicleTypeController {

    private final VehicleTypeService vehicleTypeService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VehicleType> create(@RequestBody VehicleType vt) {
        return ResponseEntity.ok(vehicleTypeService.save(vt));
    }

    @GetMapping
    public ResponseEntity<List<VehicleType>> getAll() {
        return ResponseEntity.ok(vehicleTypeService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleType> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(vehicleTypeService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VehicleType> update(@PathVariable Integer id, @RequestBody VehicleType vt) {
        return ResponseEntity.ok(vehicleTypeService.update(id, vt));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        vehicleTypeService.delete(id);
        return ResponseEntity.ok().build();
    }
}
