package com.backend.service;

import com.backend.model.Payment;
import com.backend.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {

    private final PaymentRepository repository;

    public PaymentService(PaymentRepository repository) {
        this.repository = repository;
    }

    public Payment createPayment(Payment payment) {
        payment.setTransactionDate(LocalDateTime.now());
        return repository.save(payment);
    }

    public List<Payment> getAllPayments() {
        return repository.findAll();
    }
}