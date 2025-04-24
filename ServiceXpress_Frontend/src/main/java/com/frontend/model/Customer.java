package com.frontend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Customer {
    private Long id;

    private String username;
    private String email;
    private String phoneNumber;
    private String password;
    private boolean active;
    private String role; // To match the backend's role field (optional, can be ignored if not displayed)

}