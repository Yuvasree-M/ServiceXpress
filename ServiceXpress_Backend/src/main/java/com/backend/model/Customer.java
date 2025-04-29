package com.backend.model;

import com.backend.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
public class Customer extends BaseUser {
    @Enumerated(EnumType.STRING)
    @NotNull
    private Role role = Role.CUSTOMER;

    public Customer() {}

    public Customer(String username, String email, String phoneNumber, String password) {
        setUsername(username);
        setEmail(email);
        setPhoneNumber(phoneNumber);
        setPassword(password);
    }

    public Role getRole() {
        return role;
    }
}