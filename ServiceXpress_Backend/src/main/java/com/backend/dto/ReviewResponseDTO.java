// In com.backend.dto.ReviewResponseDTO.java
package com.backend.dto;

import java.time.LocalDateTime;

public class ReviewResponseDTO {
    private Long id;
    private String customerName;
    private Integer rating;
    private String message;
    private LocalDateTime createdAt;
    private String servicePackageName; // New field

    public ReviewResponseDTO(Long id, String customerName, Integer rating, String message, 
                            LocalDateTime createdAt, String servicePackageName) {
        this.id = id;
        this.customerName = customerName;
        this.rating = rating;
        this.message = message;
        this.createdAt = createdAt;
        this.servicePackageName = servicePackageName;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public Integer getRating() { return rating; }
    public void setRating(Integer rating) { this.rating = rating; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public String getServicePackageName() { return servicePackageName; }
    public void setServicePackageName(String servicePackageName) { this.servicePackageName = servicePackageName; }
}