package com.backend.service;

import com.backend.model.ServicePackage;
import com.backend.dto.ServicePackageDTO;
import com.backend.model.VehicleType;
import com.backend.dto.VehicleTypeDTO;
import com.backend.repository.ServicePackageRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ServicePackageService {

    private static final Logger logger = LoggerFactory.getLogger(ServicePackageService.class);

    private final ServicePackageRepository servicePackageRepository;

    // Fetch all service packages
    public List<ServicePackageDTO> findAll() {
        List<ServicePackage> packages = servicePackageRepository.findAll();
        List<ServicePackageDTO> dtos = packages.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        logger.info("Fetched {} service packages: {}", dtos.size(), dtos);
        return dtos;
    }

    // Fetch a service package by ID
    public ServicePackageDTO findById(Long id) {
        ServicePackage servicePackage = servicePackageRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("ServicePackage not found with ID: {}", id);
                    return new RuntimeException("ServicePackage not found with ID: " + id);
                });
        ServicePackageDTO dto = toDTO(servicePackage);
        logger.info("Fetched service package by ID {}: {}", id, dto);
        return dto;
    }

    // Save a new service package
    public ServicePackageDTO save(ServicePackageDTO servicePackageDTO) {
        if (servicePackageDTO.getPackageName() == null || servicePackageDTO.getPackageName().trim().isEmpty()) {
            logger.error("Cannot save service package with empty packageName");
            throw new IllegalArgumentException("Package name cannot be empty");
        }
        if (servicePackageDTO.getVehicleType() == null || servicePackageDTO.getVehicleType().getId() == null) {
            logger.error("Cannot save service package with invalid vehicleType");
            throw new IllegalArgumentException("Vehicle type is required");
        }
        ServicePackage servicePackage = toEntity(servicePackageDTO);
        ServicePackage saved = servicePackageRepository.save(servicePackage);
        ServicePackageDTO result = toDTO(saved);
        logger.info("Saved service package: {}", result);
        return result;
    }

    // Update an existing service package
    public ServicePackageDTO update(Long id, ServicePackageDTO updatedPackageDTO) {
        ServicePackage existingPackage = servicePackageRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("ServicePackage not found with ID: {}", id);
                    return new RuntimeException("ServicePackage not found with ID: " + id);
                });
        if (updatedPackageDTO.getPackageName() == null || updatedPackageDTO.getPackageName().trim().isEmpty()) {
            logger.error("Cannot update service package with empty packageName");
            throw new IllegalArgumentException("Package name cannot be empty");
        }
        if (updatedPackageDTO.getVehicleType() == null || updatedPackageDTO.getVehicleType().getId() == null) {
            logger.error("Cannot update service package with invalid vehicleType");
            throw new IllegalArgumentException("Vehicle type is required");
        }
        existingPackage.setPackageName(updatedPackageDTO.getPackageName());
        existingPackage.setDescription(updatedPackageDTO.getDescription());
        existingPackage.setPrice(updatedPackageDTO.getPrice());
        existingPackage.setVehicleType(new VehicleType(
                updatedPackageDTO.getVehicleType().getId(),
                updatedPackageDTO.getVehicleType().getTypeName(),
                null // Avoid serializing vehicleModels
        ));
        ServicePackage updated = servicePackageRepository.save(existingPackage);
        ServicePackageDTO result = toDTO(updated);
        logger.info("Updated service package: {}", result);
        return result;
    }

    // Delete a service package by ID
    public void delete(Long id) {
        if (!servicePackageRepository.existsById(id)) {
            logger.error("Cannot delete service package with ID: {} - not found", id);
            throw new RuntimeException("ServicePackage not found with ID: " + id);
        }
        servicePackageRepository.deleteById(id);
        logger.info("Deleted service package with ID: {}", id);
    }

    private ServicePackageDTO toDTO(ServicePackage servicePackage) {
        return new ServicePackageDTO(
                servicePackage.getId(),
                servicePackage.getPackageName(),
                servicePackage.getDescription(),
                servicePackage.getPrice(),
                new VehicleTypeDTO(servicePackage.getVehicleType().getId(), servicePackage.getVehicleType().getTypeName())
        );
    }

    private ServicePackage toEntity(ServicePackageDTO dto) {
        ServicePackage servicePackage = new ServicePackage();
        servicePackage.setId(dto.getId());
        servicePackage.setPackageName(dto.getPackageName());
        servicePackage.setDescription(dto.getDescription());
        servicePackage.setPrice(dto.getPrice());
        servicePackage.setVehicleType(new VehicleType(
                dto.getVehicleType().getId(),
                dto.getVehicleType().getTypeName(),
                null // Avoid serializing vehicleModels
        ));
        return servicePackage;
    }
}