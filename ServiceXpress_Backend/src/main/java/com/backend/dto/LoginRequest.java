package com.backend.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String identifier;
    private String password;
}