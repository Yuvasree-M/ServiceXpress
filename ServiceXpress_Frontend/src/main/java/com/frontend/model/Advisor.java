package com.frontend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Advisor {
    private Long id;
    private String cityName;
    private String centerName;
    private String username;
    private String email;
    private String phoneNumber;
    private String password;
    private Boolean active;
}