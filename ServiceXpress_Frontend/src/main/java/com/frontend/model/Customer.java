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
    private String name;
    private String initials;
    private String email;
    private String mobile;
}