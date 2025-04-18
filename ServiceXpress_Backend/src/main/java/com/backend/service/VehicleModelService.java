package com.backend.service;

import com.backend.model.VehicleModel;
import com.backend.repository.VehicleModelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VehicleModelService {

    @Autowired
    private VehicleModelRepository vehicleModelRepository;

    public List<VehicleModel> findAll() {
        return vehicleModelRepository.findAll();
    }

    public VehicleModel findById(Integer id) {
        return vehicleModelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("VehicleModel not found with ID: " + id));
    }

    public VehicleModel save(VehicleModel vehicleModel) {
        return vehicleModelRepository.save(vehicleModel);
    }

    public VehicleModel update(Integer id, VehicleModel updatedModel) {
        VehicleModel existingModel = findById(id);
        existingModel.setModelName(updatedModel.getModelName());
        existingModel.setVehicleType(updatedModel.getVehicleType());
        return vehicleModelRepository.save(existingModel);
    }

    public void delete(Integer id) {
        vehicleModelRepository.deleteById(id);
    }
}
