package com.backend.dto;

public class PaymentRequest {
    private Long amount;
    private String currency;
    // Getters and Setters
    public Long getAmount() { return amount; }
    public void setAmount(Long amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
