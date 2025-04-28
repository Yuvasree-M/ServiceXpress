package com.backend.service;

import com.backend.dto.*;
import com.backend.dto.DashboardData;
import com.backend.model.*;
import com.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private BookingRequestRepository bookingRequestRepository;

    @Autowired
    private BookingStatusRepository bookingStatusRepository;

    @Autowired
    private AdvisorRepository advisorRepository;

    @Autowired
    private ServiceCenterRepository serviceCenterRepository;

    @Autowired
    private VehicleTypeRepository vehicleTypeRepository;

    @Autowired
    private VehicleModelRepository vehicleModelRepository;

    public DashboardData getDashboardData() {
        List<BookingRequest> pending = bookingRequestRepository.findByStatus("PENDING");
        List<BookingRequest> inProgress = bookingRequestRepository.findByStatus("IN_PROGRESS");
        List<BookingRequest> completed = bookingRequestRepository.findByStatus("COMPLETED");

        List<VehicleDue> vehiclesDue = pending.stream()
                .map(this::mapToVehicleDue)
                .collect(Collectors.toList());

        List<VehicleUnderService> vehiclesUnderService = inProgress.stream()
                .map(this::mapToVehicleUnderService)
                .collect(Collectors.toList());

        List<VehicleCompleted> vehiclesCompleted = completed.stream()
                .map(this::mapToVehicleCompleted)
                .collect(Collectors.toList());

        List<Advisor> advisors = advisorRepository.findAll().stream()
                .filter(Advisor::getActive)
                .collect(Collectors.toList());

        List<AdvisorDTO> advisorDTOs = advisors.stream()
                .map(a -> new AdvisorDTO(a.getId(), a.getUsername(), a.getCenterName()))
                .collect(Collectors.toList());

        return new DashboardData(
                vehiclesDue.size(),
                vehiclesUnderService.size(),
                vehiclesCompleted.size(),
                "Admin User",
                vehiclesDue,
                vehiclesUnderService,
                vehiclesCompleted,
                advisorDTOs
        );
    }

    private VehicleDue mapToVehicleDue(BookingRequest br) {
        ServiceCenter sc = serviceCenterRepository.findById(br.getServiceCenterId()).orElse(new ServiceCenter());
        VehicleType vt = vehicleTypeRepository.findById(br.getVehicleTypeId()).orElse(new VehicleType());
        VehicleModel vm = vehicleModelRepository.findById(br.getVehicleModelId()).orElse(new VehicleModel());

        String vehicleType = vt.getTypeName() != null ? vt.getTypeName() : "Unknown";
        String vehicleModel = vm.getModelName() != null ? vm.getModelName() : "Unknown";
        String serviceCenter = sc.getCenterName() != null ? sc.getCenterName() : "Unknown";

        return new VehicleDue(
                br.getId(),
                br.getCustomerName(),
                vehicleType,
                vehicleModel,
                br.getServices(),
                serviceCenter,
                br.getServiceCenterId(),
                br.getRequestedDate(),
                br.getStatus()
        );
    }

    private VehicleUnderService mapToVehicleUnderService(BookingRequest br) {
        ServiceCenter sc = serviceCenterRepository.findById(br.getServiceCenterId()).orElse(new ServiceCenter());
        VehicleType vt = vehicleTypeRepository.findById(br.getVehicleTypeId()).orElse(new VehicleType());
        VehicleModel vm = vehicleModelRepository.findById(br.getVehicleModelId()).orElse(new VehicleModel());
        BookingStatus bs = bookingStatusRepository.findByBookingRequestId(br.getId());

        String vehicleType = vt.getTypeName() != null ? vt.getTypeName() : "Unknown";
        String vehicleModel = vm.getModelName() != null ? vm.getModelName() : "Unknown";
        String serviceCenter = sc.getCenterName() != null ? sc.getCenterName() : "Unknown";
        String serviceAdvisor = bs != null ? bs.getServiceAdvisor() : "None";

        return new VehicleUnderService(
                br.getId(),
                br.getCustomerName(),
                vehicleType,
                vehicleModel,
                serviceCenter,
                serviceAdvisor,
                br.getServices(),
                br.getStatus()
        );
    }

    private VehicleCompleted mapToVehicleCompleted(BookingRequest br) {
        ServiceCenter sc = serviceCenterRepository.findById(br.getServiceCenterId()).orElse(new ServiceCenter());
        VehicleType vt = vehicleTypeRepository.findById(br.getVehicleTypeId()).orElse(new VehicleType());
        VehicleModel vm = vehicleModelRepository.findById(br.getVehicleModelId()).orElse(new VehicleModel());
        BookingStatus bs = bookingStatusRepository.findByBookingRequestId(br.getId());

        String vehicleType = vt.getTypeName() != null ? vt.getTypeName() : "Unknown";
        String vehicleModel = vm.getModelName() != null ? vm.getModelName() : "Unknown";
        String serviceCenter = sc.getCenterName() != null ? sc.getCenterName() : "Unknown";
        String serviceAdvisor = bs != null ? bs.getServiceAdvisor() : "None";
        String billOfMaterials = bs != null ? bs.getBillOfMaterials() : "";

        return new VehicleCompleted(
                br.getId(),
                br.getCustomerName(),
                vehicleType,
                vehicleModel,
                serviceCenter,
                serviceAdvisor,
                br.getServices(),
                bs != null ? bs.getCompletedDate() : null,
                br.getStatus(),
                billOfMaterials,
                bs != null && bs.getPaymentRequested(),
                bs != null && bs.getPaymentReceived()
        );
    }
}