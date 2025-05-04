package com.backend.service;

import com.backend.dto.BookingResponseDTO;
import com.backend.dto.CustomerDashboardDTO;
import com.backend.model.*;
import com.backend.repository.*;
import com.backend.service.EmailService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
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

    public BookingRequest createBooking(BookingRequest booking) {
        LocalDateTime now = LocalDateTime.now();
        booking.setCreatedAt(now);
        booking.setUpdatedAt(now);
        booking.setId(null);
        booking.setStatus("PENDING");
        BookingRequest savedBooking = repository.save(booking);

        String vehicleTypeId = savedBooking.getVehicleTypeId() != null ? String.valueOf(savedBooking.getVehicleTypeId()) : "Unknown";
        String vehicleTypeName = "Unknown";
        String vehicleModelId = savedBooking.getVehicleModelId() != null ? String.valueOf(savedBooking.getVehicleModelId()) : "Unknown";
        String vehicleModelName = "Unknown";
        String serviceCenterId = savedBooking.getServiceCenterId() != null ? String.valueOf(savedBooking.getServiceCenterId()) : "Unknown";
        String serviceCenterName = "Unknown";

        try {
            if (savedBooking.getVehicleTypeId() != null) {
                Optional<VehicleType> vehicleType = vehicleTypeRepository.findById(savedBooking.getVehicleTypeId());
                if (vehicleType.isPresent()) {
                    vehicleTypeName = vehicleType.get().getName();
                }
            }

            if (savedBooking.getVehicleModelId() != null) {
                Optional<VehicleModel> vehicleModel = vehicleModelRepository.findById(savedBooking.getVehicleModelId());
                if (vehicleModel.isPresent()) {
                    vehicleModelName = vehicleModel.get().getModelName();
                }
            }

            if (savedBooking.getServiceCenterId() != null) {
                Optional<ServiceCenter> serviceCenter = serviceCenterRepository.findById(savedBooking.getServiceCenterId());
                if (serviceCenter.isPresent()) {
                    serviceCenterName = serviceCenter.get().getCenterName() + " (" + serviceCenter.get().getCityName() + ")";
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching details for email, booking ID " + savedBooking.getId() + ": " + e.getMessage());
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
        } catch (Exception e) {
            System.err.println("Failed to send booking confirmation email for booking ID " + savedBooking.getId() + ": " + e.getMessage());
        }

        return savedBooking;
    }

    public List<BookingRequest> getBookingsByCustomerId(Long customerId) {
        return repository.findByCustomerId(customerId);
    }

    public Optional<BookingRequest> getBookingById(Long id) {
        return repository.findById(id);
    }

    public BookingRequest updateBooking(Long id, BookingRequest booking) {
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
        if (!repository.existsById(id)) {
            throw new RuntimeException("Booking not found with id: " + id);
        }
        repository.deleteById(id);
    }

    public List<BookingResponseDTO> getPendingBookings() {
        List<BookingRequest> pendingBookings = repository.findByStatus("PENDING");
        return mapToBookingResponseDTOs(pendingBookings);
    }

    public List<BookingResponseDTO> getInProgressBookings() {
        List<BookingRequest> inProgressBookings = repository.findByStatus("IN_PROGRESS");
        return mapToBookingResponseDTOs(inProgressBookings);
    }

    @Transactional
    public BookingRequest assignServiceAdvisor(Long bookingId, Long advisorId) {
        BookingRequest booking = repository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with id: " + bookingId));
        Advisor advisor = advisorRepository.findById(advisorId)
                .orElseThrow(() -> new IllegalArgumentException("Advisor not found with id: " + advisorId));
        Optional<BookingAdvisorMapping> existingMapping = bookingAdvisorMappingRepository.findByBookingId(bookingId);
        if (existingMapping.isPresent()) {
            throw new IllegalStateException("Advisor already assigned to booking id: " + bookingId);
        }
        BookingAdvisorMapping mapping = BookingAdvisorMapping.builder()
                .bookingId(bookingId)
                .advisorId(advisorId)
                .build();
        bookingAdvisorMappingRepository.save(mapping);
        booking.setStatus("IN_PROGRESS");
        booking.setUpdatedAt(LocalDateTime.now());
        return repository.save(booking);
    }

    public List<Advisor> getAllAdvisors() {
        return advisorRepository.findAll();
    }

    public CustomerDashboardDTO getDashboardData(Long customerId) {
        List<BookingRequest> bookings = repository.findByCustomerId(customerId);
        CustomerDashboardDTO dto = new CustomerDashboardDTO();
        List<ServiceStatus> ongoingServices = new ArrayList<>();
        List<ServiceHistory> serviceHistory = new ArrayList<>();

        for (BookingRequest booking : bookings) {
            String serviceCenterName = "Unknown";
            String vehicleTypeName = "Unknown";
            String vehicleModelName = "Unknown";

            try {
                if (booking.getServiceCenterId() != null) {
                    Optional<ServiceCenter> serviceCenter = serviceCenterRepository.findById(booking.getServiceCenterId());
                    if (serviceCenter.isPresent()) {
                        serviceCenterName = serviceCenter.get().getCenterName() + " (" + serviceCenter.get().getCityName() + ")";
                    }
                }

                if (booking.getVehicleTypeId() != null) {
                    Optional<VehicleType> vehicleType = vehicleTypeRepository.findById(booking.getVehicleTypeId());
                    if (vehicleType.isPresent()) {
                        vehicleTypeName = vehicleType.get().getName();
                    }
                }

                if (booking.getVehicleModelId() != null) {
                    Optional<VehicleModel> vehicleModel = vehicleModelRepository.findById(booking.getVehicleModelId());
                    if (vehicleModel.isPresent()) {
                        vehicleModelName = vehicleModel.get().getModelName();
                    }
                }
            } catch (Exception e) {
                System.err.println("Error fetching details for booking ID " + booking.getId() + ": " + e.getMessage());
                continue; // Skip this booking and continue processing others
            }

            Double cost = calculateCost(booking.getServices());

            if (List.of("PENDING", "IN_PROGRESS", "Ready for Pickup").contains(booking.getStatus())) {
                ServiceStatus status = new ServiceStatus();
                status.setId(booking.getId());
                status.setServiceCenterName(serviceCenterName);
                status.setVehicleTypeName(vehicleTypeName);
                status.setVehicleModelName(vehicleModelName);
                status.setRegistration(booking.getVehicleRegistrationNumber());
                status.setService(booking.getServices());
                status.setCost(cost);
                status.setStatus(booking.getStatus());
                ongoingServices.add(status);
            } else if ("COMPLETED".equals(booking.getStatus())) {
                ServiceHistory history = new ServiceHistory();
                history.setId(booking.getId());
                history.setDate(booking.getUpdatedAt().toString());
                history.setServiceCenterName(serviceCenterName);
                history.setVehicleTypeName(vehicleTypeName);
                history.setVehicleModelName(vehicleModelName);
                history.setWorkDone(booking.getServices());
                history.setCost(cost);
                history.setStatus(booking.getStatus());
                history.setTransactionId("TXN" + booking.getId());
                serviceHistory.add(history);
            }
        }

        dto.setOngoingServices(ongoingServices);
        dto.setServiceHistory(serviceHistory);
        return dto;
    }

    @Transactional
    public BookingRequest markBookingCompleted(Long bookingId) {
        BookingRequest booking = repository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));
        if (!"COMPLETED".equals(booking.getStatus())) {
            booking.setStatus("COMPLETED");
            booking.setUpdatedAt(LocalDateTime.now());
            booking = repository.save(booking);
        }
        return booking;
    }

    public Map<String, Object> getReceiptData(Long bookingId) {
        BookingRequest booking = repository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));
        if (!"COMPLETED".equals(booking.getStatus())) {
            throw new IllegalStateException("Receipt available only for completed bookings");
        }

        Map<String, Object> receiptData = new HashMap<>();
        receiptData.put("bookingId", booking.getId());
        receiptData.put("customerId", booking.getCustomerId());
        receiptData.put("customerName", booking.getCustomerName());
        receiptData.put("customerEmail", booking.getCustomerEmail());
        receiptData.put("vehicleRegistrationNumber", booking.getVehicleRegistrationNumber());
        receiptData.put("services", booking.getServices());
        receiptData.put("cost", calculateCost(booking.getServices()));
        receiptData.put("transactionId", "TXN" + booking.getId());
        receiptData.put("completionDate", booking.getUpdatedAt().toString());

        String serviceCenterName = "Unknown";
        String vehicleTypeName = "Unknown";
        String vehicleModelName = "Unknown";

        try {
            if (booking.getServiceCenterId() != null) {
                Optional<ServiceCenter> serviceCenter = serviceCenterRepository.findById(booking.getServiceCenterId());
                if (serviceCenter.isPresent()) {
                    serviceCenterName = serviceCenter.get().getCenterName() + " (" + serviceCenter.get().getCityName() + ")";
                }
            }

            if (booking.getVehicleTypeId() != null) {
                Optional<VehicleType> vehicleType = vehicleTypeRepository.findById(booking.getVehicleTypeId());
                if (vehicleType.isPresent()) {
                    vehicleTypeName = vehicleType.get().getName();
                }
            }

            if (booking.getVehicleModelId() != null) {
                Optional<VehicleModel> vehicleModel = vehicleModelRepository.findById(booking.getVehicleModelId());
                if (vehicleModel.isPresent()) {
                    vehicleModelName = vehicleModel.get().getModelName();
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching details for receipt ID " + booking.getId() + ": " + e.getMessage());
        }

        receiptData.put("serviceCenterName", serviceCenterName);
        receiptData.put("vehicleTypeName", vehicleTypeName);
        receiptData.put("vehicleModelName", vehicleModelName);

        return receiptData;
    }

    private Double calculateCost(String services) {
        if (services == null || services.isEmpty()) return 0.0;
        Map<String, Double> serviceCosts = new HashMap<>();
        serviceCosts.put("Basic Wash", 50.0);
        serviceCosts.put("Oil Change", 100.0);
        serviceCosts.put("Full Service", 200.0);
        return Arrays.stream(services.split(","))
                .map(String::trim)
                .filter(serviceCosts::containsKey)
                .mapToDouble(serviceCosts::get)
                .sum();
    }

    private List<BookingResponseDTO> mapToBookingResponseDTOs(List<BookingRequest> bookings) {
        return bookings.stream().map(booking -> {
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
                System.err.println("Error fetching details for booking ID " + booking.getId() + ": " + e.getMessage());
            }

            return new BookingResponseDTO(booking, serviceCenterFormatted, vehicleTypeFormatted, vehicleModelFormatted);
        }).collect(Collectors.toList());
    }
}