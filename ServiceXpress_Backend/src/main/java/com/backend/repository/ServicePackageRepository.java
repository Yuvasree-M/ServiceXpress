package com.backend.repository;

import com.backend.model.ServicePackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServicePackageRepository extends JpaRepository<ServicePackage, Integer> {
    List<ServicePackage> findByVehicleTypeId(Integer vehicleTypeId);
}