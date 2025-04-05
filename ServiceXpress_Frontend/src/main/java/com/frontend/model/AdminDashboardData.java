package com.frontend.model;

public class AdminDashboardData {
    private int totalUsers;
    private int totalServices;
    private double totalRevenue;

    public AdminDashboardData() {}
    public AdminDashboardData(int totalUsers, int totalServices, double totalRevenue) {
        this.totalUsers = totalUsers;
        this.totalServices = totalServices;
        this.totalRevenue = totalRevenue;
    }

    public int getTotalUsers() { return totalUsers; }
    public void setTotalUsers(int totalUsers) { this.totalUsers = totalUsers; }
    public int getTotalServices() { return totalServices; }
    public void setTotalServices(int totalServices) { this.totalServices = totalServices; }
    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }
}