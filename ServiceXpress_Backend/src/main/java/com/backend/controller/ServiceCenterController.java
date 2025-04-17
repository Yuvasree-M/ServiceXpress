package com.backend.controller;

import com.backend.model.ServiceCenter;
import com.backend.service.ServiceCenterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/service-centers")
public class ServiceCenterController {

    @Autowired
    private ServiceCenterService serviceCenterService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<ServiceCenter> getAllCenters() {
        return serviceCenterService.getAllCenters();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<ServiceCenter> getCenterById(@PathVariable Long id) {
        ServiceCenter center = serviceCenterService.getCenterById(id);
        if (center == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(center);
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ServiceCenter> createCenter(@RequestBody ServiceCenter serviceCenter) {
        ServiceCenter createdCenter = serviceCenterService.createCenter(serviceCenter);
        return new ResponseEntity<>(createdCenter, HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<ServiceCenter> updateCenter(@PathVariable Long id, @RequestBody ServiceCenter serviceCenter) {
        ServiceCenter updatedCenter = serviceCenterService.updateCenter(id, serviceCenter);
        return new ResponseEntity<>(updatedCenter, HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCenter(@PathVariable Long id) {
        serviceCenterService.deleteCenter(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
