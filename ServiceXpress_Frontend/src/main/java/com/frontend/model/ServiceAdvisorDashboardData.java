package com.frontend.model;

import java.util.List;

public class ServiceAdvisorDashboardData {
    private int pendingServices;
    private int completedServices;
    private List<String> assignedTasks;

    public ServiceAdvisorDashboardData() {}
    public ServiceAdvisorDashboardData(int pendingServices, int completedServices, List<String> assignedTasks) {
        this.pendingServices = pendingServices;
        this.completedServices = completedServices;
        this.assignedTasks = assignedTasks;
    }

    public int getPendingServices() { return pendingServices; }
    public void setPendingServices(int pendingServices) { this.pendingServices = pendingServices; }
    public int getCompletedServices() { return completedServices; }
    public void setCompletedServices(int completedServices) { this.completedServices = completedServices; }
    public List<String> getAssignedTasks() { return assignedTasks; }
    public void setAssignedTasks(List<String> assignedTasks) { this.assignedTasks = assignedTasks; }
}