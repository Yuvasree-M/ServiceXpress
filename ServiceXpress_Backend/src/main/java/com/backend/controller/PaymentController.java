package com.backend.controller;

import com.backend.model.Payment;
import com.backend.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService service;

    public PaymentController(PaymentService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Payment> createPayment(@RequestBody Payment payment) {
        return ResponseEntity.ok(service.createPayment(payment));
    }

    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments() {
        return ResponseEntity.ok(service.getAllPayments());
    }
}