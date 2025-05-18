package com.backend.dto;

import java.util.List;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter

public class BillOfMaterialDTO {
    private Long bookingId;
    private String customerName;
    private String advisorName;
    private String serviceName;
    private List<Material> materials;
    private Double total;

@Getter
@Setter
    public static class Material {
        private String materialName;
        private Integer quantity;
        private Double price;

    }
}