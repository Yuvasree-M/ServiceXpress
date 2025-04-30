package com.backend.repository;

import com.backend.model.VehicleModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VehicleModelRepository extends JpaRepository<VehicleModel, Integer> {
    List<VehicleModel> findByVehicleTypeId(Integer vehicleTypeId);
}