package com.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
public class ServiceAdvisor extends BaseUser {
    @Enumerated(EnumType.STRING)
    private Role role = Role.SERVICE_ADVISOR;

    public ServiceAdvisor() {}
    
    public ServiceAdvisor(String username, String email, String phoneNumber, String password) {
        setUsername(username);
        setEmail(email);
        setPhoneNumber(phoneNumber);
        setPassword(password);
    }

    public Role getRole() { return role; }
}