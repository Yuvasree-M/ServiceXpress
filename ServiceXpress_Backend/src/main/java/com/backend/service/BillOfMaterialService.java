package com.backend.service;

import com.backend.dto.BillOfMaterialDTO;
import com.backend.model.BillOfMaterial;
import com.backend.repository.BillOfMaterialRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BillOfMaterialService {

    private static final Logger logger = LoggerFactory.getLogger(BillOfMaterialService.class);

    @Autowired
    private BillOfMaterialRepository billOfMaterialRepository;

    @Autowired
    private ObjectMapper objectMapper;

    public void saveBillOfMaterial(BillOfMaterialDTO bomDTO) {
        try {
            BillOfMaterial bom = new BillOfMaterial();
            bom.setBookingId(bomDTO.getBookingId());
            bom.setCustomerName(bomDTO.getCustomerName());
            bom.setAdvisorName(bomDTO.getAdvisorName());
            bom.setServiceName(bomDTO.getServiceName());
            bom.setTotal(bomDTO.getTotal());

            // Convert materials list to JSON string
            String materialsJson = objectMapper.writeValueAsString(bomDTO.getMaterials());
            bom.setMaterials(materialsJson);

            billOfMaterialRepository.save(bom);
        } catch (Exception e) {
            logger.error("Error saving BillOfMaterial for booking ID {}: {}", bomDTO.getBookingId(), e.getMessage(), e);
            throw new RuntimeException("Failed to save BillOfMaterial", e);
        }
    }
}