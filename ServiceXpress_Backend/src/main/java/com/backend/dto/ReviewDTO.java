package com.backend.dto;

public class ReviewDTO {
    private Long bookingId;
    private Long customerId;
    private Integer rating;
    private String message;

    // Getters
    public Long getBookingId() {
        return bookingId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public Integer getRating() {
        return rating;
    }

    public String getMessage() {
        return message;
    }

    // Setters
    public void setBookingId(Long bookingId) {
        this.bookingId = bookingId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}