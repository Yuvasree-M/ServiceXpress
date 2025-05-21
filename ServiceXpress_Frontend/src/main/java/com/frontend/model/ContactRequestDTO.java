package com.frontend.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ContactRequestDTO {
    private Long customerId; // Nullable for guest users
    private String name;
    private String email;
    private String subject;
    private String message;
}