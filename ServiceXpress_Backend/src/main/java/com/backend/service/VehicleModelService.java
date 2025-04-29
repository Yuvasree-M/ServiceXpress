package com.backend.service;

import com.backend.model.VehicleModel;
import com.backend.dto.VehicleModelDTO;
import com.backend.model.VehicleType;
import com.backend.dto.VehicleTypeDTO;
import com.backend.repository.VehicleModelRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleModelService {

    private static final Logger logger = LoggerFactory.getLogger(VehicleModelService.class);

    private final VehicleModelRepository vehicleModelRepository;

    public List<VehicleModelDTO> findAll() {
        List<VehicleModel> models = vehicleModelRepository.findAll();
        List<VehicleModelDTO> dtos = models.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        logger.info("Fetched {} vehicle models: {}", dtos.size(), dtos);
        return dtos;
    }

    public VehicleModelDTO findById(Integer id) {
        VehicleModel model = vehicleModelRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("VehicleModelDTO not found with ID: {}", id);
                    return new RuntimeException("VehicleModelDTO not found with ID: " + id);
                });
        VehicleModelDTO dto = toDTO(model);
        logger.info("Fetched vehicle model by ID {}: {}", id, dto);
        return dto;
    }

    public VehicleModelDTO save(VehicleModelDTO vehicleModelDTO) {
        if (vehicleModelDTO.getModelName() == null || vehicleModelDTO.getModelName().trim().isEmpty()) {
            logger.error("Cannot save vehicle model with empty modelName");
            throw new IllegalArgumentException("Model name cannot be empty");
        }
        if (vehicleModelDTO.getVehicleType() == null || vehicleModelDTO.getVehicleType().getId() == null) {
            logger.error("Cannot save vehicle model with invalid vehicleType");
            throw new IllegalArgumentException("Vehicle type is required");
        }
        VehicleModel model = toEntity(vehicleModelDTO);
        VehicleModel saved = vehicleModelRepository.save(model);
        VehicleModelDTO result = toDTO(saved);
        logger.info("Saved vehicle model: {}", result);
        return result;
    }

    public VehicleModelDTO update(Integer id, VehicleModelDTO vehicleModelDTO) {
        VehicleModel existingModel = vehicleModelRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("VehicleModelDTO not found with ID: {}", id);
                    return new RuntimeException("VehicleModelDTO not found with ID: " + id);
                });
        if (vehicleModelDTO.getModelName() == null || vehicleModelDTO.getModelName().trim().isEmpty()) {
            logger.error("Cannot update vehicle model with empty modelName");
            throw new IllegalArgumentException("Model name cannot be empty");
        }
        if (vehicleModelDTO.getVehicleType() == null || vehicleModelDTO.getVehicleType().getId() == null) {
            logger.error("Cannot update vehicle model with invalid vehicleType");
            throw new IllegalArgumentException("Vehicle type is required");
        }
        existingModel.setModelName(vehicleModelDTO.getModelName());
        existingModel.setVehicleType(new VehicleType(
                vehicleModelDTO.getVehicleType().getId(),
                vehicleModelDTO.getVehicleType().getName()
        ));
        VehicleModel updated = vehicleModelRepository.save(existingModel);
        VehicleModelDTO result = toDTO(updated);
        logger.info("Updated vehicle model: {}", result);
        return result;
    }

    public void delete(Integer id) {
        if (!vehicleModelRepository.existsById(id)) {
            logger.error("Cannot delete vehicle model with ID: {} - not found", id);
            throw new RuntimeException("VehicleModelDTO not found with ID: " + id);
        }
        vehicleModelRepository.deleteById(id);
        logger.info("Deleted vehicle model with ID: {}", id);
    }

    private VehicleModelDTO toDTO(VehicleModel model) {
        return new VehicleModelDTO(
                model.getVehicleModelId(),
                model.getModelName(),
                new VehicleTypeDTO(model.getVehicleType().getId(), model.getVehicleType().getName())
        );
    }

    private VehicleModel toEntity(VehicleModelDTO dto) {
        VehicleModel model = new VehicleModel();
        model.setVehicleModelId(dto.getId());
        model.setModelName(dto.getModelName());
        model.setVehicleType(new VehicleType(
                dto.getVehicleType().getId(),
                dto.getVehicleType().getName()
        ));
        return model;
    }
}