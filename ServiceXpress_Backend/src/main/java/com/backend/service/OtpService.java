package com.backend.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class OtpService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.phone.number}")
    private String twilioPhoneNumber;

    private final Map<String, String> otpStore = new HashMap<>(); // Temporary in-memory store

    public String generateOtp(String phoneNumber) {
        String otp = String.format("%06d", new Random().nextInt(999999)); // 6-digit OTP
        otpStore.put(phoneNumber, otp);
        System.out.println("Generated OTP for " + phoneNumber + ": " + otp); // Log OTP for debugging
        return otp;
    }

    public void sendOtp(String phoneNumber, String otp) {
        try {
            // Validate phone number format (E.164)
            if (!phoneNumber.matches("\\+?[1-9]\\d{1,14}")) {
                throw new IllegalArgumentException("Invalid phone number format: " + phoneNumber);
            }

            Twilio.init(accountSid, authToken);
            Message message = Message.creator(
                    new PhoneNumber(phoneNumber), // To
                    new PhoneNumber(twilioPhoneNumber), // From
                    "Your ServiceXpress OTP is: " + otp
            ).create();
            System.out.println("OTP sent to " + phoneNumber + ": " + message.getSid());
        } catch (Exception e) {
            System.err.println("Failed to send OTP to " + phoneNumber + ": " + e.getMessage());
            throw new RuntimeException("Failed to send OTP: " + e.getMessage(), e);
        }
    }

    public boolean verifyOtp(String phoneNumber, String otp) {
        String storedOtp = otpStore.get(phoneNumber);
        if (storedOtp != null && storedOtp.equals(otp)) {
            otpStore.remove(phoneNumber); // Clear OTP after verification
            return true;
        }
        return false;
    }
}