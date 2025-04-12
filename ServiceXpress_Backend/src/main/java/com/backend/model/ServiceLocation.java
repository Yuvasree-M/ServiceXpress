package com.backend.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "service_locations")
public class ServiceLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "city_name", nullable = false)
    private String cityName;

    @Column(name = "center_name", nullable = false)
    private String centerName;

    @Column(name = "advisor_username", nullable = false, unique = true)
    private String advisorUsername;

    @Column(name = "advisor_email", unique = true)
    private String advisorEmail;

    @Column(name = "advisor_phone_number")
    private String advisorPhoneNumber;

    @Column(name = "advisor_password")
    private String advisorPassword;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role = Role.SERVICE_ADVISOR;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }
    public String getCenterName() { return centerName; }
    public void setCenterName(String centerName) { this.centerName = centerName; }
    public String getAdvisorUsername() { return advisorUsername; }
    public void setAdvisorUsername(String advisorUsername) { this.advisorUsername = advisorUsername; }
    public String getAdvisorEmail() { return advisorEmail; }
    public void setAdvisorEmail(String advisorEmail) { this.advisorEmail = advisorEmail; }
    public String getAdvisorPhoneNumber() { return advisorPhoneNumber; }
    public void setAdvisorPhoneNumber(String advisorPhoneNumber) { this.advisorPhoneNumber = advisorPhoneNumber; }
    public String getAdvisorPassword() { return advisorPassword; }
    public void setAdvisorPassword(String advisorPassword) { this.advisorPassword = advisorPassword; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ServiceLocation)) return false;
        ServiceLocation that = (ServiceLocation) o;
        return Objects.equals(advisorUsername, that.advisorUsername);
    }

    @Override
    public int hashCode() {
        return Objects.hash(advisorUsername);
    }
}