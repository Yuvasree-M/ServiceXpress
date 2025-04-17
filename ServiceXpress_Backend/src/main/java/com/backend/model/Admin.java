package com.backend.model;

import com.backend.enums.Role;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
public class Admin extends BaseUser {
    @Enumerated(EnumType.STRING)
    private Role role = Role.ADMIN;

    public Admin() {}
    
    public Admin(String username, String email, String phoneNumber, String password) {
        setUsername(username);
        setEmail(email);
        setPhoneNumber(phoneNumber);
        setPassword(password);
    }

    public Role getRole() { return role; }
}
