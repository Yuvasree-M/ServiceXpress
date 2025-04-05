package com.frontend.model;

import java.util.List;

public class CustomerDashboardData {
    private int bookedServices;
    private String lastServiceDate;
    private List<String> upcomingAppointments;

    public CustomerDashboardData() {}
    public CustomerDashboardData(int bookedServices, String lastServiceDate, List<String> upcomingAppointments) {
        this.bookedServices = bookedServices;
        this.lastServiceDate = lastServiceDate;
        this.upcomingAppointments = upcomingAppointments;
    }

    public int getBookedServices() { return bookedServices; }
    public void setBookedServices(int bookedServices) { this.bookedServices = bookedServices; }
    public String getLastServiceDate() { return lastServiceDate; }
    public void setLastServiceDate(String lastServiceDate) { this.lastServiceDate = lastServiceDate; }
    public List<String> getUpcomingAppointments() { return upcomingAppointments; }
    public void setUpcomingAppointments(List<String> upcomingAppointments) { this.upcomingAppointments = upcomingAppointments; }
}