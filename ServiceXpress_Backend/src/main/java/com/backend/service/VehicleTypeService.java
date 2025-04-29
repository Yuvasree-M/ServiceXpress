package com.backend.service;

import com.backend.model.VehicleType;
import com.backend.dto.VehicleTypeDTO;
import com.backend.repository.VehicleTypeRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleTypeService {

    private static final Logger logger = LoggerFactory.getLogger(VehicleTypeService.class);

    private final VehicleTypeRepository vehicleTypeRepository;

    public List<VehicleTypeDTO> findAll() {
        List<VehicleType> types = vehicleTypeRepository.findAll();
        List<VehicleTypeDTO> dtos = types.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        logger.info("Fetched {} vehicle types: {}", dtos.size(), dtos);
        return dtos;
    }

    public Optional<VehicleTypeDTO> findById(Integer id) {
        Optional<VehicleTypeDTO> dto = vehicleTypeRepository.findById(id)
                .map(this::toDTO);
        logger.info("Fetched vehicle type by ID {}: {}", id, dto);
        return dto;
    }

    public VehicleTypeDTO save(VehicleTypeDTO vehicleTypeDTO) {
        VehicleType vehicleType = toEntity(vehicleTypeDTO);
        VehicleType saved = vehicleTypeRepository.save(vehicleType);
        VehicleTypeDTO result = toDTO(saved);
        logger.info("Saved vehicle type: {}", result);
        return result;
    }

    public void delete(Integer id) {
        vehicleTypeRepository.deleteById(id);
        logger.info("Deleted vehicle type with ID: {}", id);
    }

    private VehicleTypeDTO toDTO(VehicleType vehicleType) {
        return new VehicleTypeDTO(vehicleType.getId(), vehicleType.getName());
    }

    private VehicleType toEntity(VehicleTypeDTO dto) {
        VehicleType vehicleType = new VehicleType();
        vehicleType.setId(dto.getId());
        vehicleType.setName(dto.getName());
        return vehicleType;
    }
}