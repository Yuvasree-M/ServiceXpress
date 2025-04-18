package com.backend.controller;

import com.backend.model.Advisor;
import com.backend.service.AdvisorService; // Import your service
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/advisor")
public class AdvisorController {

    @Autowired
    private AdvisorService advisorService; // Injecting the AdvisorService to handle logic for Advisor

    // Fetch advisor by username
    @GetMapping("/findByUsername/{username}")
    public ResponseEntity<Advisor> getAdvisorByUsername(@PathVariable String username) {
        return advisorService.findAdvisorByUsername(username)
                .map(ResponseEntity::ok)  // If present, return the Advisor
                .orElseGet(() -> ResponseEntity.notFound().build());  // If not, return 404
    }

    // Fetch advisor by email
    @GetMapping("/findByEmail/{email}")
    public ResponseEntity<Advisor> getAdvisorByEmail(@PathVariable String email) {
        return advisorService.findAdvisorByEmail(email)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // Fetch advisor by phone number
    @GetMapping("/findByPhone/{phoneNumber}")
    public ResponseEntity<Advisor> getAdvisorByPhoneNumber(@PathVariable String phoneNumber) {
        return advisorService.findAdvisorByPhoneNumber(phoneNumber)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Advisor> create(@RequestBody Advisor advisor) {
        return ResponseEntity.ok(advisorService.save(advisor));
    }

    @GetMapping
    public ResponseEntity<List<Advisor>> getAll() {
        return ResponseEntity.ok(advisorService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Advisor> getById(@PathVariable Long id) {
        return advisorService.findById(id)
                .map(ResponseEntity::ok)  // If found, return the Advisor
                .orElseGet(() -> ResponseEntity.notFound().build());  // If not, return 404
    }

    @PutMapping("/{id}")
    public ResponseEntity<Advisor> update(@PathVariable Long id, @RequestBody Advisor advisor) {
        return ResponseEntity.ok(advisorService.update(id, advisor));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        advisorService.delete(id);
        return ResponseEntity.ok().build();
    }
}
