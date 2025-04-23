package com.backend.model;

import java.util.List;

public class DashboardData {
	private int totalUsers;
	private int totalServices;
	private double totalRevenue;
	private int bookedServices;
	private String lastServiceDate;
	private List<String> upcomingAppointments;
	private int pendingServices;
	private int completedServices;
	private List<String> assignedTasks;

	public int getTotalUsers() {
		return totalUsers;
	}

	public void setTotalUsers(int totalUsers) {
		this.totalUsers = totalUsers;
	}

	public int getTotalServices() {
		return totalServices;
	}

	public void setTotalServices(int totalServices) {
		this.totalServices = totalServices;
	}

	public double getTotalRevenue() {
		return totalRevenue;
	}

	public void setTotalRevenue(double totalRevenue) {
		this.totalRevenue = totalRevenue;
	}

	public int getBookedServices() {
		return bookedServices;
	}

	public void setBookedServices(int bookedServices) {
		this.bookedServices = bookedServices;
	}

	public String getLastServiceDate() {
		return lastServiceDate;
	}

	public void setLastServiceDate(String lastServiceDate) {
		this.lastServiceDate = lastServiceDate;
	}

	public List<String> getUpcomingAppointments() {
		return upcomingAppointments;
	}

	public void setUpcomingAppointments(List<String> upcomingAppointments) {
		this.upcomingAppointments = upcomingAppointments;
	}

	public int getPendingServices() {
		return pendingServices;
	}

	public void setPendingServices(int pendingServices) {
		this.pendingServices = pendingServices;
	}

	public int getCompletedServices() {
		return completedServices;
	}

	public void setCompletedServices(int completedServices) {
		this.completedServices = completedServices;
	}

	public List<String> getAssignedTasks() {
		return assignedTasks;
	}

	public void setAssignedTasks(List<String> assignedTasks) {
		this.assignedTasks = assignedTasks;
	}
}