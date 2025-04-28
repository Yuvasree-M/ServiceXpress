package com.backend.controller;

import com.backend.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping("/{id}/assign-advisor")
    public void assignAdvisor(@PathVariable Long id, @RequestParam String advisorUsername) {
        bookingService.assignAdvisor(id, advisorUsername);
    }

    @PostMapping("/{id}/complete")
    public void completeBooking(@PathVariable Long id, @RequestParam String billOfMaterials) {
        bookingService.completeBooking(id, billOfMaterials);
    }
}