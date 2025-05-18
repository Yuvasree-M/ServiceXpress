package com.backend.dto;

import lombok.Data;

@Data
public class UserDetailsResponse {
    private Long customerId;
    private String name;
    private String email;
    private String phoneNumber;
    private Long advisorId;

}
