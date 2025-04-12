package com.backend.controller;

import com.backend.model.ServiceLocation;
import com.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/locations")
public class ServiceLocationController {

    private final UserService userService;

    public ServiceLocationController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ServiceLocation> createLocation(@RequestBody ServiceLocation location) {
        return ResponseEntity.ok(userService.createServiceLocation(location));
    }

    @GetMapping
    public ResponseEntity<List<ServiceLocation>> getAllLocations() {
        return ResponseEntity.ok(userService.getAllServiceLocations());
    }
}