package com.backend.service;

import com.backend.model.VehicleModel;
import com.backend.repository.VehicleModelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class VehicleModelService {

    @Autowired
    private VehicleModelRepository vehicleModelRepository;

    public List<VehicleModel> getAllVehicleModels() {
        return vehicleModelRepository.findAll();
    }

    public Optional<VehicleModel> getVehicleModelById(Integer id) {
        return vehicleModelRepository.findById(id);
    }

    public VehicleModel createVehicleModel(VehicleModel vehicleModel) {
        return vehicleModelRepository.save(vehicleModel);
    }

    public VehicleModel updateVehicleModel(VehicleModel vehicleModel) {
        return vehicleModelRepository.save(vehicleModel);
    }

    public void deleteVehicleModel(Integer id) {
        vehicleModelRepository.deleteById(id);
    }
}
