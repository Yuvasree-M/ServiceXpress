package com.backend.dto;

import lombok.Data;

@Data
public class PaymentVerificationRequest {
    private String bookingId;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private String razorpaySignature;

}
