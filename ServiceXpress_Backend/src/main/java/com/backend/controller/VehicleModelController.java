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

import com.backend.model.VehicleModel;
import com.backend.service.VehicleModelService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/vehicle-models")
@RequiredArgsConstructor
public class VehicleModelController {

    private final VehicleModelService modelService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VehicleModel> create(@RequestBody VehicleModel model) {
        return ResponseEntity.ok(modelService.save(model));
    }

    @GetMapping
    public ResponseEntity<List<VehicleModel>> getAll() {
        return ResponseEntity.ok(modelService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VehicleModel> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(modelService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VehicleModel> update(@PathVariable Integer id, @RequestBody VehicleModel model) {
        return ResponseEntity.ok(modelService.update(id, model));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        modelService.delete(id);
        return ResponseEntity.ok().build();
    }
}
