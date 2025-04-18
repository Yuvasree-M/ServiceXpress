package com.backend.service;

import com.backend.model.ServicePackage;
import com.backend.repository.ServicePackageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServicePackageService {

    @Autowired
    private ServicePackageRepository servicePackageRepository;

    // Fetch all service packages
    public List<ServicePackage> findAll() {
        return servicePackageRepository.findAll();
    }

    // Fetch a service package by ID
    public ServicePackage findById(Integer id) {
        return servicePackageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("ServicePackage not found with ID: " + id));
    }

    // Save a new service package
    public ServicePackage save(ServicePackage servicePackage) {
        return servicePackageRepository.save(servicePackage);
    }

    // Update an existing service package
    public ServicePackage update(Integer id, ServicePackage updatedPackage) {
        ServicePackage existingPackage = findById(id);
        existingPackage.setPackageName(updatedPackage.getPackageName());
        existingPackage.setDescription(updatedPackage.getDescription());
        existingPackage.setPrice(updatedPackage.getPrice());
        // Update any other fields as needed
        return servicePackageRepository.save(existingPackage);
    }

    // Delete a service package by ID
    public void delete(Integer id) {
        servicePackageRepository.deleteById(id);
    }
}
