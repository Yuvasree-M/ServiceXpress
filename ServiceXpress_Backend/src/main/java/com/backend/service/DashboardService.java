package com.backend.service;

import com.backend.config.JwtUtil;
import com.backend.model.*;
import com.backend.repository.*;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    private final ServiceRepository serviceRepo;
    private final AdminRepository adminRepo;
    private final AdvisorRequestRepository advisorRequestRepo;
    private final PaymentRepository paymentRepo;
    private final JwtUtil jwtUtil;

    public DashboardService(ServiceRepository serviceRepo, 
                          AdminRepository adminRepo,
                          AdvisorRequestRepository advisorRequestRepo,
                          PaymentRepository paymentRepo,
                          JwtUtil jwtUtil) {
        this.serviceRepo = serviceRepo;
        this.adminRepo = adminRepo;
        this.advisorRequestRepo = advisorRequestRepo;
        this.paymentRepo = paymentRepo;
        this.jwtUtil = jwtUtil;
    }

    public DashboardData getAdminDashboardData(String token) {
        String username = jwtUtil.extractUsername(token);
        Admin admin = adminRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        List<com.backend.model.Service> services = serviceRepo.findAll();
        List<Payment> payments = paymentRepo.findAll();

        DashboardData data = new DashboardData();
        data.setDueCount((int) services.stream().filter(s -> "Requested".equals(s.getStatus())).count());
        data.setServicingCount((int) services.stream().filter(s -> "In Progress".equals(s.getStatus())).count());
        data.setCompletedCount((int) services.stream().filter(s -> "Completed".equals(s.getStatus())).count());
        data.setAdvisorRequestsCount(advisorRequestRepo.countPendingRequests());
        data.setProfileName(admin.getUsername());
        
        data.setVehiclesDue(services.stream()
                .filter(s -> "Requested".equals(s.getStatus()))
                .map(this::mapToVehicleDue)
                .collect(Collectors.toList()));
        data.setVehiclesUnderService(services.stream()
                .filter(s -> "In Progress".equals(s.getStatus()))
                .map(this::mapToVehicleUnderService)
                .collect(Collectors.toList()));
        data.setVehiclesCompleted(services.stream()
                .filter(s -> "Completed".equals(s.getStatus()))
                .map(s -> mapToVehicleCompleted(s, payments))
                .collect(Collectors.toList()));
        data.setAdvisorRequests(advisorRequestRepo.findAll().stream()
                .map(this::mapToAdvisorRequest)
                .collect(Collectors.toList()));
        
        return data;
    }

    private VehicleDue mapToVehicleDue(com.backend.model.Service s) {
        return new VehicleDue(
                s.getVehicle().getOwner().getUsername(),
                s.getVehicle().getVehicleModel(),
                s.getVehicle().getVehicleType(),
                s.getServiceType(),
                s.getServiceCenter().getLocation(),
                s.getServiceAdvisor() != null ? s.getServiceAdvisor().getId() : null,
                Collections.emptyList(),
                s.getRequestedDate(),
                s.getDueDate(),
                s.getStatus()
        );
    }

    private VehicleUnderService mapToVehicleUnderService(com.backend.model.Service s) {
        return new VehicleUnderService(
                s.getVehicle().getOwner().getUsername(),
                s.getVehicle().getVehicleType(),
                s.getVehicle().getVehicleModel(),
                s.getServiceCenter().getName(),
                s.getServiceAdvisor() != null ? s.getServiceAdvisor().getUsername() : "Unassigned",
                s.getServiceType(),
                s.getStatus()
        );
    }

    private VehicleCompleted mapToVehicleCompleted(com.backend.model.Service s, List<Payment> payments) {
        Payment payment = payments.stream()
                .filter(p -> p.getService().getId().equals(s.getId()))
                .findFirst()
                .orElse(null);
        return new VehicleCompleted(
                s.getId(),
                s.getVehicle().getOwner().getUsername(),
                s.getVehicle().getVehicleType(),
                s.getVehicle().getVehicleModel(),
                s.getServiceCenter().getName(),
                s.getServiceAdvisor() != null ? s.getServiceAdvisor().getUsername() : "Unassigned",
                s.getServiceType(),
                s.getCompletedDate(),
                s.getStatus(),
                payment != null && Boolean.TRUE.equals(payment.getPaymentRequested()),
                payment != null && Boolean.TRUE.equals(payment.getPaymentReceived())
        );
    }

    private AdvisorRequest mapToAdvisorRequest(com.backend.model.AdvisorRequest ar) {
        return new AdvisorRequest(
                ar.getId(),
                ar.getAdvisor().getUsername(),
                ar.getService().getServiceType(),
                ar.getService().getVehicle().getOwner().getUsername(),
                ar.getService().getVehicle().getVehicleType(),
                ar.getService().getVehicle().getVehicleModel(),
                ar.getService().getServiceCenter().getName(),
                ar.getStatus()
        );
    }
}