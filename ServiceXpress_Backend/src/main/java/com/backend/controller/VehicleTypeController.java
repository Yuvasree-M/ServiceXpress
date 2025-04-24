
package com.backend.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.backend.dto.VehicleTypeDTO;
import com.backend.service.VehicleTypeService;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/vehicle-type")
@RequiredArgsConstructor
public class VehicleTypeController {

    private final VehicleTypeService vehicleTypeService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VehicleTypeDTO> create(@RequestBody VehicleTypeDTO vehicleTypeDTO) {
        if (vehicleTypeDTO.getTypeName() == null || vehicleTypeDTO.getTypeName().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        VehicleTypeDTO saved = vehicleTypeService.save(vehicleTypeDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<List<VehicleTypeDTO>> getAll() {
        List<VehicleTypeDTO> vehicleTypes = vehicleTypeService.findAll();
        return vehicleTypes.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(vehicleTypes);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<VehicleTypeDTO> getById(@PathVariable Long id) {
        Optional<VehicleTypeDTO> vehicleType = vehicleTypeService.findById(id);
        return vehicleType.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VehicleTypeDTO> update(@PathVariable Long id, @RequestBody VehicleTypeDTO vehicleTypeDTO) {
        if (vehicleTypeDTO.getTypeName() == null || vehicleTypeDTO.getTypeName().trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Optional<VehicleTypeDTO> existingType = vehicleTypeService.findById(id);
        return existingType.map(existing -> {
            vehicleTypeDTO.setId(id);
            return ResponseEntity.ok(vehicleTypeService.save(vehicleTypeDTO));
        }).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        Optional<VehicleTypeDTO> existingType = vehicleTypeService.findById(id);
        if (existingType.isPresent()) {
            vehicleTypeService.delete(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}