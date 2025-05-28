package com.backend.service;

import com.backend.dto.ReviewDTO;
import com.backend.model.BookingRequest;
import com.backend.model.Customer;
import com.backend.model.Review;
import com.backend.repository.BookingRequestRepository;
import com.backend.repository.CustomerRepository;
import com.backend.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private BookingRequestRepository bookingRequestRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Transactional
    public void createReview(ReviewDTO reviewDTO) {
        // Validate inputs
        if (reviewDTO.getBookingId() == null || reviewDTO.getCustomerId() == null) {
            throw new IllegalArgumentException("Booking ID and Customer ID are required");
        }
        if (reviewDTO.getRating() < 1 || reviewDTO.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        // Check if booking exists
        Optional<BookingRequest> bookingOpt = bookingRequestRepository.findById(reviewDTO.getBookingId());
        if (!bookingOpt.isPresent()) {
            throw new IllegalStateException("Booking not found with ID: " + reviewDTO.getBookingId());
        }
        BookingRequest booking = bookingOpt.get();

        // Verify customer owns the booking
        if (!booking.getCustomerId().equals(reviewDTO.getCustomerId())) {
            throw new IllegalStateException("Customer ID " + reviewDTO.getCustomerId() + " is not authorized to review booking ID " + reviewDTO.getBookingId());
        }

        // Check booking status
        if (!List.of("COMPLETED", "COMPLETED_PAID").contains(booking.getStatus())) {
            throw new IllegalStateException("Booking must be in COMPLETED or COMPLETED_PAID status, current status: " + booking.getStatus());
        }

        // Check if review already exists for this booking
        if (reviewRepository.findByBookingId(reviewDTO.getBookingId()).isPresent()) {
            throw new IllegalStateException("A review already exists for booking ID: " + reviewDTO.getBookingId());
        }

        // Get customer
        Optional<Customer> customerOpt = customerRepository.findById(reviewDTO.getCustomerId());
        if (!customerOpt.isPresent()) {
            throw new IllegalStateException("Customer not found with ID: " + reviewDTO.getCustomerId());
        }
        Customer customer = customerOpt.get();

        // Create review
        Review review = new Review();
        review.setBookingId(booking.getId());
        review.setCustomerId(customer.getId());
        review.setRating(reviewDTO.getRating());
        String message = reviewDTO.getMessage() != null ? reviewDTO.getMessage().trim() : null;
        review.setMessage(message != null && !message.isEmpty() ? message : null);
        review.setCreatedAt(LocalDateTime.now());

        reviewRepository.save(review);
    }
}