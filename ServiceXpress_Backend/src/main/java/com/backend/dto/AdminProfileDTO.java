package com.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AdminProfileDTO {
    private String username;
    private String email;
    private String phoneNumber;
    private String role;
}