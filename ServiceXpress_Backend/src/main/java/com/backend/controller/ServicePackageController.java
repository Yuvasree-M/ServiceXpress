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

import com.backend.model.ServicePackage;
import com.backend.service.ServicePackageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/service-packages")
@RequiredArgsConstructor
public class ServicePackageController {

    private final ServicePackageService packageService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServicePackage> create(@RequestBody ServicePackage sp) {
        return ResponseEntity.ok(packageService.save(sp));
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<ServicePackage>> getAll() {
        return ResponseEntity.ok(packageService.findAll());
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ServicePackage> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(packageService.findById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ServicePackage> update(@PathVariable Integer id, @RequestBody ServicePackage sp) {
        return ResponseEntity.ok(packageService.update(id, sp));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        packageService.delete(id);
        return ResponseEntity.ok().build();
    }
}