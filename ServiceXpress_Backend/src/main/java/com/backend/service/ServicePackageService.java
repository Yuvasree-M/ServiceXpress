package com.backend.service;

import com.backend.model.ServicePackage;
import com.backend.repository.ServicePackageRepository;
import com.backend.repository.VehicleTypeRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ServicePackageService {

    private final ServicePackageRepository servicePackageRepository;
    private final VehicleTypeRepository vehicleTypeRepository;

    public ServicePackageService(ServicePackageRepository servicePackageRepository, VehicleTypeRepository vehicleTypeRepository) {
        this.servicePackageRepository = servicePackageRepository;
        this.vehicleTypeRepository = vehicleTypeRepository;
    }

    // Fetch all service packages
    public List<ServicePackage> findAll() {
        return servicePackageRepository.findAll();
    }

    // Fetch a service package by ID
    public ServicePackage findById(Integer id) {
        return servicePackageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ServicePackage not found with ID: " + id));
    }

    // Save a new service package
    public ServicePackage save(ServicePackage servicePackage) {
        validateVehicleType(servicePackage.getVehicleType().getId());
        return servicePackageRepository.save(servicePackage);
    }

    // Update an existing service package
    public ServicePackage update(Integer id, ServicePackage updatedPackage) {
        ServicePackage existingPackage = findById(id);
        validateVehicleType(updatedPackage.getVehicleType().getId());
        existingPackage.setPackageName(updatedPackage.getPackageName());
        existingPackage.setDescription(updatedPackage.getDescription());
        existingPackage.setPrice(updatedPackage.getPrice());
        existingPackage.setVehicleType(updatedPackage.getVehicleType());
        return servicePackageRepository.save(existingPackage);
    }

    // Delete a service package by ID
    public void delete(Integer id) {
        if (!servicePackageRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ServicePackage not found with ID: " + id);
        }
        servicePackageRepository.deleteById(id);
    }

    // Validate vehicle type existence
    private void validateVehicleType(Integer vehicleTypeId) {
        if (!vehicleTypeRepository.existsById(vehicleTypeId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "VehicleType not found with ID: " + vehicleTypeId);
        }
    }
}