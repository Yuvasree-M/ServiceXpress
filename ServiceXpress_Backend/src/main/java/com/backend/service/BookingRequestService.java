package com.backend.service;

import com.backend.dto.BookingResponseDTO;
import com.backend.model.Advisor;
import com.backend.model.BookingAdvisorMapping;
import com.backend.model.BookingRequest;
import com.backend.model.ServiceCenter;
import com.backend.model.VehicleModel;
import com.backend.model.VehicleType;
import com.backend.repository.AdvisorRepository;
import com.backend.repository.BookingAdvisorMappingRepository;
import com.backend.repository.BookingRequestRepository;
import com.backend.repository.ServiceCenterRepository;
import com.backend.repository.VehicleModelRepository;
import com.backend.repository.VehicleTypeRepository;
import com.backend.service.EmailService;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BookingRequestService {

    @Autowired
    private BookingRequestRepository repository;

    @Autowired
    private VehicleTypeRepository vehicleTypeRepository;

    @Autowired
    private VehicleModelRepository vehicleModelRepository;

    @Autowired
    private ServiceCenterRepository serviceCenterRepository;

    @Autowired
    private AdvisorRepository advisorRepository;

    @Autowired
    private BookingAdvisorMappingRepository bookingAdvisorMappingRepository;

    @Autowired
    private EmailService emailService;
    
    private static final Logger logger = LoggerFactory.getLogger(BookingRequestService.class);

    public AdvisorRepository getAdvisorRepository() {
        return advisorRepository;
    }

    public BookingAdvisorMappingRepository getBookingAdvisorMappingRepository() {
        return bookingAdvisorMappingRepository;
    }

    public BookingRequest createBooking(BookingRequest booking) {
        logger.info("Creating new booking for customer: {}", booking.getCustomerName());
        LocalDateTime now = LocalDateTime.now();
        booking.setCreatedAt(now);
        booking.setUpdatedAt(now);
        booking.setId(null);
        BookingRequest savedBooking = repository.save(booking);

        String vehicleTypeId = String.valueOf(savedBooking.getVehicleTypeId());
        String vehicleTypeName = "Unknown";
        String vehicleModelId = String.valueOf(savedBooking.getVehicleModelId());
        String vehicleModelName = "Unknown";
        String serviceCenterId = String.valueOf(savedBooking.getServiceCenterId());
        String serviceCenterName = "Unknown";

        try {
            Optional<VehicleType> vehicleType = vehicleTypeRepository.findById(savedBooking.getVehicleTypeId());
            if (vehicleType.isPresent()) {
                vehicleTypeName = vehicleType.get().getName();
            }

            Optional<VehicleModel> vehicleModel = vehicleModelRepository.findById(savedBooking.getVehicleModelId());
            if (vehicleModel.isPresent()) {
                vehicleModelName = vehicleModel.get().getModelName();
            }

            Optional<ServiceCenter> serviceCenter = serviceCenterRepository.findById(savedBooking.getServiceCenterId());
            if (serviceCenter.isPresent()) {
                serviceCenterName = serviceCenter.get().getCenterName() + " (" + serviceCenter.get().getCityName() + ")";
            }
        } catch (Exception e) {
            logger.error("Error fetching details for email: {}", e.getMessage(), e);
        }

        try {
            emailService.sendBookingConfirmationEmail(
                savedBooking.getCustomerEmail(),
                savedBooking.getCustomerName(),
                savedBooking,
                vehicleTypeId,
                vehicleTypeName,
                vehicleModelId,
                vehicleModelName,
                serviceCenterId,
                serviceCenterName
            );
            logger.info("Booking confirmation email sent to: {}", savedBooking.getCustomerEmail());
        } catch (Exception e) {
            logger.error("Failed to send booking confirmation email: {}", e.getMessage(), e);
        }

        return savedBooking;
    }

    public List<BookingRequest> getBookingsByCustomerId(Long customerId) {
        logger.info("Fetching bookings for customerId: {}", customerId);
        return repository.findByCustomerId(customerId);
    }

    public Optional<BookingRequest> getBookingById(Long id) {
        logger.info("Fetching booking by id: {}", id);
        return repository.findById(id);
    }

    public BookingRequest updateBooking(Long id, BookingRequest booking) {
        logger.info("Updating booking with id: {}", id);
        BookingRequest existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + id));
        existing.setCustomerName(booking.getCustomerName());
        existing.setCustomerPhone(booking.getCustomerPhone());
        existing.setCustomerEmail(booking.getCustomerEmail());
        existing.setAddress(booking.getAddress());
        existing.setServices(booking.getServices());
        existing.setPickDropOption(booking.getPickDropOption());
        existing.setPickupAddress(booking.getPickupAddress());
        existing.setDropAddress(booking.getDropAddress());
        existing.setPickupDropoffOption(booking.getPickupDropoffOption());
        existing.setRequestedDate(booking.getRequestedDate());
        existing.setStatus(booking.getStatus());
        existing.setVehicleRegistrationNumber(booking.getVehicleRegistrationNumber());
        existing.setUpdatedAt(LocalDateTime.now());
        return repository.save(existing);
    }

    public void deleteBooking(Long id) {
        logger.info("Deleting booking with id: {}", id);
        if (!repository.existsById(id)) {
            throw new RuntimeException("Booking not found with id: " + id);
        }
        repository.deleteById(id);
    }
    
    public List<BookingResponseDTO> getPendingBookings() {
        logger.info("Fetching pending bookings");
        List<BookingRequest> pendingBookings = repository.findByStatus("PENDING");
        return pendingBookings.stream().map(booking -> {
            String vehicleTypeFormatted = booking.getVehicleTypeId() != null ? booking.getVehicleTypeId() + " (Unknown)" : "Unknown";
            String vehicleModelFormatted = booking.getVehicleModelId() != null ? booking.getVehicleModelId() + " (Unknown)" : "Unknown";
            String serviceCenterFormatted = booking.getServiceCenterId() != null ? booking.getServiceCenterId() + " (Unknown)" : "Unknown";

            try {
                if (booking.getVehicleTypeId() != null) {
                    Optional<VehicleType> vehicleType = vehicleTypeRepository.findById(booking.getVehicleTypeId());
                    if (vehicleType.isPresent()) {
                        vehicleTypeFormatted = booking.getVehicleTypeId() + " (" + vehicleType.get().getName() + ")";
                    }
                }

                if (booking.getVehicleModelId() != null) {
                    Optional<VehicleModel> vehicleModel = vehicleModelRepository.findById(booking.getVehicleModelId());
                    if (vehicleModel.isPresent()) {
                        vehicleModelFormatted = booking.getVehicleModelId() + " (" + vehicleModel.get().getModelName() + ")";
                    }
                }

                if (booking.getServiceCenterId() != null) {
                    Optional<ServiceCenter> serviceCenter = serviceCenterRepository.findById(booking.getServiceCenterId());
                    if (serviceCenter.isPresent()) {
                        serviceCenterFormatted = booking.getServiceCenterId() + " (" + serviceCenter.get().getCenterName() + ")";
                    }
                }
            } catch (Exception e) {
                logger.error("Error fetching details for booking ID {}: {}", booking.getId(), e.getMessage(), e);
            }

            return new BookingResponseDTO(booking, serviceCenterFormatted, vehicleTypeFormatted, vehicleModelFormatted);
        }).collect(Collectors.toList());
    }
    
    @Transactional
    public BookingRequest assignServiceAdvisor(Long bookingId, Long advisorId) {
        logger.info("Assigning advisor {} to booking {}", advisorId, bookingId);
        BookingRequest booking = repository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with id: " + bookingId));

        Advisor advisor = advisorRepository.findById(advisorId)
                .orElseThrow(() -> new IllegalArgumentException("Advisor not found with id: " + advisorId));

        Optional<BookingAdvisorMapping> existingMapping = bookingAdvisorMappingRepository.findByBookingId(bookingId);
        if (existingMapping.isPresent()) {
            throw new IllegalStateException("Advisor already assigned to booking id: " + bookingId);
        }

        BookingAdvisorMapping mapping = new BookingAdvisorMapping(bookingId, advisorId);
        bookingAdvisorMappingRepository.save(mapping);

        booking.setStatus("ASSIGNED");
        booking.setUpdatedAt(LocalDateTime.now());
        return repository.save(booking);
    }

    @Transactional
    public BookingRequest startService(Long bookingId) {
        logger.info("Starting service for bookingId: {}", bookingId);
        BookingRequest booking = repository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with id: " + bookingId));

        if (!"ASSIGNED".equals(booking.getStatus())) {
            throw new IllegalStateException("Booking must be in ASSIGNED status to start service. Current status: " + booking.getStatus());
        }

        Optional<BookingAdvisorMapping> mapping = bookingAdvisorMappingRepository.findByBookingId(bookingId);
        if (!mapping.isPresent()) {
            throw new IllegalStateException("No advisor assigned to booking id: " + bookingId);
        }

        booking.setStatus("IN_PROGRESS");
        booking.setUpdatedAt(LocalDateTime.now());
        return repository.save(booking);
    }

    public List<BookingResponseDTO> getAssignedBookings() {
        logger.info("Fetching assigned bookings");
        List<BookingRequest> assignedBookings = repository.findByStatus("ASSIGNED");
        return assignedBookings.stream().map(booking -> {
            String vehicleTypeFormatted = booking.getVehicleTypeId() != null ? booking.getVehicleTypeId() + " (Unknown)" : "Unknown";
            String vehicleModelFormatted = booking.getVehicleModelId() != null ? booking.getVehicleModelId() + " (Unknown)" : "Unknown";
            String serviceCenterFormatted = booking.getServiceCenterId() != null ? booking.getServiceCenterId() + " (Unknown)" : "Unknown";

            try {
                if (booking.getVehicleTypeId() != null) {
                    Optional<VehicleType> vehicleType = vehicleTypeRepository.findById(booking.getVehicleTypeId());
                    if (vehicleType.isPresent()) {
                        vehicleTypeFormatted = booking.getVehicleTypeId() + " (" + vehicleType.get().getName() + ")";
                    }
                }

                if (booking.getVehicleModelId() != null) {
                    Optional<VehicleModel> vehicleModel = vehicleModelRepository.findById(booking.getVehicleModelId());
                    if (vehicleModel.isPresent()) {
                        vehicleModelFormatted = booking.getVehicleModelId() + " (" + vehicleModel.get().getModelName() + ")";
                    }
                }

                if (booking.getServiceCenterId() != null) {
                    Optional<ServiceCenter> serviceCenter = serviceCenterRepository.findById(booking.getServiceCenterId());
                    if (serviceCenter.isPresent()) {
                        serviceCenterFormatted = booking.getServiceCenterId() + " (" + serviceCenter.get().getCenterName() + ")";
                    }
                }
            } catch (Exception e) {
                logger.error("Error fetching details for booking ID {}: {}", booking.getId(), e.getMessage(), e);
            }

            return new BookingResponseDTO(booking, serviceCenterFormatted, vehicleTypeFormatted, vehicleModelFormatted);
        }).collect(Collectors.toList());
    }

    public List<BookingResponseDTO> getAssignedBookingsForAdvisor(Long advisorId) {
        logger.info("Fetching assigned bookings for advisorId: {}", advisorId);
        List<BookingAdvisorMapping> mappings = bookingAdvisorMappingRepository.findByAdvisorId(advisorId);
        if (mappings == null || mappings.isEmpty()) {
            logger.debug("No bookings assigned to advisorId: {}", advisorId);
            return new ArrayList<>();
        }

        List<Long> bookingIds = mappings.stream()
                .map(BookingAdvisorMapping::getBookingId)
                .collect(Collectors.toList());

        if (bookingIds.isEmpty()) {
            logger.debug("No booking IDs found for advisorId: {}", advisorId);
            return new ArrayList<>();
        }

        List<BookingRequest> assignedBookings = repository.findByIdInAndStatus(bookingIds, "ASSIGNED");
        if (assignedBookings == null || assignedBookings.isEmpty()) {
            logger.debug("No assigned bookings found for advisorId: {}", advisorId);
            return new ArrayList<>();
        }

        return assignedBookings.stream().map(booking -> {
            String vehicleTypeFormatted = booking.getVehicleTypeId() != null ? booking.getVehicleTypeId() + " (Unknown)" : "Unknown";
            String vehicleModelFormatted = booking.getVehicleModelId() != null ? booking.getVehicleModelId() + " (Unknown)" : "Unknown";
            String serviceCenterFormatted = booking.getServiceCenterId() != null ? booking.getServiceCenterId() + " (Unknown)" : "Unknown";

            try {
                if (booking.getVehicleTypeId() != null) {
                    Optional<VehicleType> vehicleType = vehicleTypeRepository.findById(booking.getVehicleTypeId());
                    if (vehicleType.isPresent()) {
                        vehicleTypeFormatted = booking.getVehicleTypeId() + " (" + vehicleType.get().getName() + ")";
                    }
                }

                if (booking.getVehicleModelId() != null) {
                    Optional<VehicleModel> vehicleModel = vehicleModelRepository.findById(booking.getVehicleModelId());
                    if (vehicleModel.isPresent()) {
                        vehicleModelFormatted = booking.getVehicleModelId() + " (" + vehicleModel.get().getModelName() + ")";
                    }
                }

                if (booking.getServiceCenterId() != null) {
                    Optional<ServiceCenter> serviceCenter = serviceCenterRepository.findById(booking.getServiceCenterId());
                    if (serviceCenter.isPresent()) {
                        serviceCenterFormatted = booking.getServiceCenterId() + " (" + serviceCenter.get().getCenterName() + ")";
                    }
                }
            } catch (Exception e) {
                logger.error("Error fetching details for booking ID {}: {}", booking.getId(), e.getMessage(), e);
            }

            return new BookingResponseDTO(booking, serviceCenterFormatted, vehicleTypeFormatted, vehicleModelFormatted);
        }).collect(Collectors.toList());
    }

    public List<BookingResponseDTO> getInProgressBookings() {
        logger.info("Fetching in-progress bookings");
        List<BookingRequest> inProgressBookings = repository.findByStatus("IN_PROGRESS");
        return inProgressBookings.stream().map(booking -> {
            String vehicleTypeFormatted = booking.getVehicleTypeId() != null ? booking.getVehicleTypeId() + " (Unknown)" : "Unknown";
            String vehicleModelFormatted = booking.getVehicleModelId() != null ? booking.getVehicleModelId() + " (Unknown)" : "Unknown";
            String serviceCenterFormatted = booking.getServiceCenterId() != null ? booking.getServiceCenterId() + " (Unknown)" : "Unknown";

            try {
                if (booking.getVehicleTypeId() != null) {
                    Optional<VehicleType> vehicleType = vehicleTypeRepository.findById(booking.getVehicleTypeId());
                    if (vehicleType.isPresent()) {
                        vehicleTypeFormatted = booking.getVehicleTypeId() + " (" + vehicleType.get().getName() + ")";
                    }
                }

                if (booking.getVehicleModelId() != null) {
                    Optional<VehicleModel> vehicleModel = vehicleModelRepository.findById(booking.getVehicleModelId());
                    if (vehicleModel.isPresent()) {
                        vehicleModelFormatted = booking.getVehicleModelId() + " (" + vehicleModel.get().getModelName() + ")";
                    }
                }

                if (booking.getServiceCenterId() != null) {
                    Optional<ServiceCenter> serviceCenter = serviceCenterRepository.findById(booking.getServiceCenterId());
                    if (serviceCenter.isPresent()) {
                        serviceCenterFormatted = booking.getServiceCenterId() + " (" + serviceCenter.get().getCenterName() + ")";
                    }
                }
            } catch (Exception e) {
                logger.error("Error fetching details for booking ID {}: {}", booking.getId(), e.getMessage(), e);
            }

            return new BookingResponseDTO(booking, serviceCenterFormatted, vehicleTypeFormatted, vehicleModelFormatted);
        }).collect(Collectors.toList());
    }

    public List<Advisor> getAllAdvisors() {
        logger.info("Fetching all advisors");
        return advisorRepository.findAll();
    }
}