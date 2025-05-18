package com.backend.dto;

import lombok.Data;

@Data
public class PaymentResponse {
    private String orderId;
    private Long amount;
    private String currency;
}
