package com.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactRequestDTO {
    private Long customerId;
    private String name;
    private String email;
    private String subject;
    private String message;
}