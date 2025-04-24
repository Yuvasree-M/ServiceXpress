package com.backend.controller;

import com.backend.model.Advisor;
import com.backend.service.AdvisorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/advisors")
@CrossOrigin(origins = "http://localhost:8082")
public class AdvisorController {

    @Autowired
    private AdvisorService advisorService;

    @GetMapping
    public List<Advisor> getAllAdvisors() {
        return advisorService.getAllAdvisors(); // Returns only active advisors
    }

    @GetMapping("/{id}")
    public ResponseEntity<Advisor> getAdvisorById(@PathVariable Long id) {
        Optional<Advisor> advisor = advisorService.getAdvisorById(id);
        return advisor.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Advisor createAdvisor(@RequestBody Advisor advisor) {
        return advisorService.createAdvisor(advisor);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Advisor> updateAdvisor(@PathVariable Long id, @RequestBody Advisor updatedAdvisor) {
        Optional<Advisor> updated = advisorService.updateAdvisor(id, updatedAdvisor);
        return updated.map(ResponseEntity::ok)
                      .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdvisor(@PathVariable Long id) {
        return advisorService.softDeleteAdvisor(id) ? ResponseEntity.ok().<Void>build() : ResponseEntity.notFound().build();
    }
}