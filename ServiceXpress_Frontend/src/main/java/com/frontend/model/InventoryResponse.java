package com.frontend.model;

import java.util.List;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InventoryResponse {
    private List<InventoryItem> items;
    private int currentPage;
    private int totalPages;
    private List<String> categories;
    // Getters & Setters
}

