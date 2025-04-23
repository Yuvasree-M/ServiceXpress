package com.backend.controller;

import com.backend.dto.ServicePackageDTO;
import com.backend.service.ServicePackageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service-packages")
@RequiredArgsConstructor
public class ServicePackageController {

    private final ServicePackageService packageService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServicePackageDTO> create(@RequestBody ServicePackageDTO sp) {
        ServicePackageDTO saved = packageService.save(sp);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<List<ServicePackageDTO>> getAll() {
        List<ServicePackageDTO> packages = packageService.findAll();
        return packages.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(packages);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    public ResponseEntity<ServicePackageDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(packageService.findById(id));
    }

    @PutMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServicePackageDTO> update(@PathVariable Long id, @RequestBody ServicePackageDTO sp) {
        return ResponseEntity.ok(packageService.update(id, sp));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        packageService.delete(id);
        return ResponseEntity.noContent().build();
    }
}