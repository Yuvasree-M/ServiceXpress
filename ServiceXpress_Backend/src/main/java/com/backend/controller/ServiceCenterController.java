package com.backend.controller;

import com.backend.model.ServiceCenter;
import com.backend.service.ServiceCenterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service-centers")
public class ServiceCenterController {

    private final ServiceCenterService service;

    public ServiceCenterController(ServiceCenterService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ServiceCenter>> getAllCenters() {
        return ResponseEntity.ok(service.getAllCenters());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ServiceCenter> getCenterById(@PathVariable Long id) {
        ServiceCenter center = service.getCenterById(id);
        return center != null ? ResponseEntity.ok(center) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<ServiceCenter> createCenter(@RequestBody ServiceCenter center) {
        return ResponseEntity.ok(service.createCenter(center));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ServiceCenter> updateCenter(@PathVariable Long id, @RequestBody ServiceCenter center) {
        return ResponseEntity.ok(service.updateCenter(id, center));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCenter(@PathVariable Long id) {
        service.deleteCenter(id);
        return ResponseEntity.ok().build();
    }
}