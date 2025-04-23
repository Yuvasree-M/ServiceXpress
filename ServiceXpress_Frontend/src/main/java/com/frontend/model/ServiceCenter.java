package com.frontend.model;

public class ServiceCenter {
    private Long id;
    private String cityName;
    private String centerName;

    public ServiceCenter() {}

    public ServiceCenter(Long id, String cityName, String centerName) {
        this.id = id;
        this.cityName = cityName;
        this.centerName = centerName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getCenterName() {
        return centerName;
    }

    public void setCenterName(String centerName) {
        this.centerName = centerName;
    }

    @Override
    public String toString() {
        return "ServiceCenter{" +
                "id=" + id +
                ", cityName='" + cityName + '\'' +
                ", centerName='" + centerName + '\'' +
                '}';
    }
}