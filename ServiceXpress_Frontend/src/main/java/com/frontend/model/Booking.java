package com.frontend.model;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Booking {
    private String name;
    private String email;
    private String mobile;
    private String location;
    private String vehicle;
    private String model;
    private String engineType;
    private List<String> services;
    private String address;
    private String pickupDropoff;
    private String pickupDropoffOption;
    private String pickupAddress;
    private String dropoffAddress;

    public Booking() {
    }
}
