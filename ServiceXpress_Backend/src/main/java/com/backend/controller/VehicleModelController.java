package com.backend.controller;

import com.backend.dto.VehicleModelDTO;
import com.backend.model.VehicleModel;
import com.backend.service.VehicleModelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicle-models")
@RequiredArgsConstructor
public class VehicleModelController {

    private final VehicleModelService modelService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VehicleModelDTO> create(@RequestBody VehicleModelDTO modelDTO) {
        VehicleModelDTO saved = modelService.save(modelDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<List<VehicleModelDTO>> getAll() {
        List<VehicleModelDTO> models = modelService.findAll();
        return models.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(models);
    }

    @GetMapping(value = "/{vehicleTypeId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<List<VehicleModelDTO>> getByVehicleTypeId(@PathVariable Integer vehicleTypeId) {
        List<VehicleModelDTO> models = modelService.findByVehicleTypeId(vehicleTypeId);
        return models.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(models);
    }

    @GetMapping(value = "/model/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<VehicleModelDTO> getById(@PathVariable Integer id) {
        try {
            VehicleModelDTO model = modelService.findById(id);
            return ResponseEntity.ok(model);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VehicleModelDTO> update(@PathVariable Integer id, @RequestBody VehicleModelDTO modelDTO) {
        try {
            VehicleModelDTO updated = modelService.update(id, modelDTO);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        try {
            modelService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
}