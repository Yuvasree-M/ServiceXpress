package com.backend.dto;

import lombok.Data;

@Data
public class PaymentRequest {
    private Long amount;
    private String currency;

}
