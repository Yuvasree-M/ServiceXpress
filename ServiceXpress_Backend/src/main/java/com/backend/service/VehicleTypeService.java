package com.backend.service;

import com.backend.model.VehicleType;
import com.backend.repository.VehicleTypeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehicleTypeService {

    @Autowired
    private VehicleTypeRepository vehicleTypeRepository;

    public List<VehicleType> findAll() {
        return vehicleTypeRepository.findAll();
    }

    public VehicleType findById(Integer id) {
        return vehicleTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("VehicleType not found with ID: " + id));
    }

    public VehicleType save(VehicleType vehicleType) {
        return vehicleTypeRepository.save(vehicleType);
    }

    public VehicleType update(Integer id, VehicleType updatedType) {
        VehicleType existingType = findById(id);
        existingType.setTypeName(updatedType.getTypeName());
        return vehicleTypeRepository.save(existingType);
    }

    public void delete(Integer id) {
        vehicleTypeRepository.deleteById(id);
    }
}
