package com.frontend.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthResponse {
    private String token;
    private String role;

    public AuthResponse() {}

    @JsonProperty("token")
    public String getToken() { return token; }
    @JsonProperty("token")
    public void setToken(String token) { this.token = token; }

    @JsonProperty("role")
    public String getRole() { return role; }
    @JsonProperty("role")
    public void setRole(String role) { this.role = role; }
}