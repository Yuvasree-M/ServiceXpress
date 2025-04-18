package com.backend.service;

import com.backend.model.ServicePackage;
import com.backend.repository.ServicePackageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ServicePackageService {

    @Autowired
    private ServicePackageRepository servicePackageRepository;

    public List<ServicePackage> getAllServicePackages() {
        return servicePackageRepository.findAll();
    }

    public Optional<ServicePackage> getServicePackageById(Integer id) {
        return servicePackageRepository.findById(id);
    }

    public ServicePackage createServicePackage(ServicePackage servicePackage) {
        return servicePackageRepository.save(servicePackage);
    }

    public ServicePackage updateServicePackage(ServicePackage servicePackage) {
        return servicePackageRepository.save(servicePackage);
    }

    public void deleteServicePackage(Integer id) {
        servicePackageRepository.deleteById(id);
    }
}
