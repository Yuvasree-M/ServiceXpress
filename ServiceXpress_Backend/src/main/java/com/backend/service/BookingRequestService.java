
package com.backend.service;

import com.backend.dto.*;
import com.backend.model.*;
import com.backend.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookingRequestService {

	@Autowired
	private CustomerRepository customerRepository;
	
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
    private BillOfMaterialRepository billOfMaterialRepository;

    @Autowired
    private BillOfMaterialService billOfMaterialService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${razorpay.key.id}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret}")
    private String razorpayKeySecret;

    private static final Logger logger = LoggerFactory.getLogger(BookingRequestService.class);

    public BookingRequestRepository getBookingRequestRepository() {
        return repository;
    }

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
            logger.error("Error fetching details for email for booking ID {}: {}", savedBooking.getId(), e.getMessage(), e);
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
            logger.error("Failed to send booking confirmation email for booking ID {}: {}", savedBooking.getId(), e.getMessage(), e);
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
        return mapToBookingResponseDTOs(pendingBookings);
    }

    public List<BookingResponseDTO> getInProgressBookings() {
        logger.info("Fetching in-progress bookings");
        List<BookingRequest> inProgressBookings = repository.findByStatus("IN_PROGRESS");
        return mapToBookingResponseDTOs(inProgressBookings);
    }

    public List<BookingResponseDTO> getCompletedBookings() {
        logger.info("Fetching completed and payment pending bookings");
        List<BookingRequest> completedBookings = repository.findByStatusIn(Arrays.asList("COMPLETED", "COMPLETED_PENDING_PAYMENT"));
        logger.info("Found {} completed or payment pending bookings", completedBookings.size());
        completedBookings.forEach(booking -> logger.info("Booking ID: {}, Status: {}", booking.getId(), booking.getStatus()));
        return mapToBookingResponseDTOs(completedBookings);
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
        return mapToBookingResponseDTOs(assignedBookings);
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

        return mapToBookingResponseDTOs(assignedBookings);
    }

    public List<Advisor> getAllAdvisors() {
        logger.info("Fetching all advisors");
        return advisorRepository.findAll();
    }

    public CustomerDashboardDTO getDashboardData(Long customerId) {
        List<BookingRequest> bookings = repository.findByCustomerId(customerId);
        CustomerDashboardDTO dto = new CustomerDashboardDTO();
        List<ServiceStatus> ongoingServices = new ArrayList<>();
        List<ServiceHistory> serviceHistory = new ArrayList<>();

        for (BookingRequest booking : bookings) {
            try {
                String serviceCenterName = booking.getServiceCenterId() != null ?
                        serviceCenterRepository.findById(booking.getServiceCenterId())
                                .map(sc -> sc.getCenterName() + " (" + sc.getCityName() + ")")
                                .orElse("Unknown") : "Unknown";
                String vehicleTypeName = booking.getVehicleTypeId() != null ?
                        vehicleTypeRepository.findById(Integer.valueOf(booking.getVehicleTypeId()))
                                .map(VehicleType::getName)
                                .orElse("Unknown") : "Unknown";
                String vehicleModelName = booking.getVehicleModelId() != null ?
                        vehicleModelRepository.findById(Integer.valueOf(booking.getVehicleModelId()))
                                .map(VehicleModel::getModelName)
                                .orElse("Unknown") : "Unknown";

                Double cost = calculateCost(booking.getServices());

                if (List.of("PENDING", "ASSIGNED", "IN_PROGRESS", "COMPLETED_PENDING_PAYMENT").contains(booking.getStatus())) {
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
                }

                if ("COMPLETED_PAID".equals(booking.getStatus()) && booking.getTransactionId() != null) {
                    ServiceHistory history = new ServiceHistory();
                    history.setId(booking.getId());
                    history.setDate(booking.getUpdatedAt().toLocalDate().toString());
                    history.setVehicleTypeName(vehicleTypeName);
                    history.setVehicleModelName(vehicleModelName);
                    history.setWorkDone(booking.getServices());
                    history.setCost(cost);
                    history.setStatus("Completed");
                    history.setTransactionId(booking.getTransactionId());
                    serviceHistory.add(history);
                }
            } catch (Exception e) {
                logger.error("Error processing booking ID {} for customer ID {}: {}", booking.getId(), customerId, e.getMessage(), e);
                continue;
            }
        }

        dto.setOngoingServices(ongoingServices);
        dto.setServiceHistory(serviceHistory);
        return dto;
    }

    public DashboardDataDTO getAdminDashboardData() {
        logger.info("Fetching admin dashboard data");
        DashboardDataDTO dashboardData = new DashboardDataDTO();

       
        List<BookingResponseDTO> pendingBookings = getPendingBookings();
        List<VehicleDueDTO> vehiclesDue = pendingBookings.stream().map(booking -> {
            VehicleDueDTO dto = new VehicleDueDTO();
            dto.setId(booking.getId());
            dto.setOwnerName(booking.getCustomerName());
            dto.setVehicleModel(booking.getVehicleModel());
            dto.setVehicleType(booking.getVehicleType());
            dto.setServiceNeeded(booking.getServices());
            dto.setLocation(booking.getServiceCenter());
            dto.setRequestedDate(booking.getRequestedDate());
            dto.setDueDate(booking.getRequestedDate().plusDays(2));
            dto.setStatus(booking.getStatus());
            List<AdvisorDTO> advisorDTOs = getAllAdvisors().stream().map(advisor -> {
                AdvisorDTO advisorDTO = new AdvisorDTO();
                advisorDTO.setId(advisor.getId());
                advisorDTO.setName(advisor.getUsername());
                return advisorDTO;
            }).collect(Collectors.toList());
            dto.setAvailableAdvisors(advisorDTOs);
            Optional<BookingAdvisorMapping> mapping = bookingAdvisorMappingRepository.findByBookingId(booking.getId());
            if (mapping.isPresent()) {
                dto.setServiceAdvisorId(mapping.get().getAdvisorId());
            }
            return dto;
        }).collect(Collectors.toList());
        dashboardData.setVehiclesDue(vehiclesDue);
        dashboardData.setDueCount(vehiclesDue.size());

       
        List<BookingResponseDTO> assignedBookings = getAssignedBookings();
        List<VehicleAssignedDTO> vehiclesAssigned = assignedBookings.stream().map(booking -> {
            VehicleAssignedDTO dto = new VehicleAssignedDTO();
            dto.setId(booking.getId());
            dto.setCustomerName(booking.getCustomerName());
            dto.setVehicleModel(booking.getVehicleModel());
            dto.setVehicleType(booking.getVehicleType());
            dto.setServicesNeeded(booking.getServices());
            Optional<BookingAdvisorMapping> mapping = bookingAdvisorMappingRepository.findByBookingId(booking.getId());
            String advisorName = "Unknown";
            if (mapping.isPresent()) {
                Optional<Advisor> advisor = advisorRepository.findById(mapping.get().getAdvisorId());
                advisorName = advisor.isPresent() ? advisor.get().getUsername() : "Unknown";
            }
            dto.setAdvisorName(advisorName);
            dto.setAssignedDate(booking.getUpdatedAt());
            dto.setStatus(booking.getStatus());
            return dto;
        }).collect(Collectors.toList());
        dashboardData.setVehiclesAssigned(vehiclesAssigned);
        dashboardData.setAssignedCount(vehiclesAssigned.size());

       
        List<BookingResponseDTO> inProgressBookings = getInProgressBookings();
        List<VehicleUnderServiceDTO> vehiclesUnderService = inProgressBookings.stream().map(booking -> {
            VehicleUnderServiceDTO dto = new VehicleUnderServiceDTO();
            dto.setId(booking.getId());
            dto.setOwnerName(booking.getCustomerName());
            dto.setVehicleType(booking.getVehicleType());
            dto.setVehicleModel(booking.getVehicleModel());
            dto.setServiceCenter(booking.getServiceCenter());
            Optional<BookingAdvisorMapping> mapping = bookingAdvisorMappingRepository.findByBookingId(booking.getId());
            String advisorName = "Unknown";
            if (mapping.isPresent()) {
                Optional<Advisor> advisor = advisorRepository.findById(mapping.get().getAdvisorId());
                advisorName = advisor.isPresent() ? advisor.get().getUsername() : "Unknown";
            }
            dto.setServiceAdvisor(advisorName);
            dto.setServiceAllocated(booking.getServices());
            dto.setStatus(booking.getStatus());
            return dto;
        }).collect(Collectors.toList());
        dashboardData.setVehiclesUnderService(vehiclesUnderService);
        dashboardData.setServicingCount(vehiclesUnderService.size());

        
        List<BookingResponseDTO> completedBookings = getCompletedBookings();
        List<VehicleCompletedDTO> vehiclesCompleted = completedBookings.stream().map(booking -> {
            VehicleCompletedDTO dto = new VehicleCompletedDTO();
            dto.setId(booking.getId());
            dto.setCustomerId(booking.getCustomerId() != null ? booking.getCustomerId() : 0L); // Prevent null
            dto.setOwnerName(booking.getCustomerName());
            dto.setVehicleType(booking.getVehicleType());
            dto.setVehicleModel(booking.getVehicleModel());
            dto.setServiceCenter(booking.getServiceCenter());
            Optional<BookingAdvisorMapping> mapping = bookingAdvisorMappingRepository.findByBookingId(booking.getId());
            String advisorName = "Unknown";
            if (mapping.isPresent()) {
                Optional<Advisor> advisor = advisorRepository.findById(mapping.get().getAdvisorId());
                advisorName = advisor.isPresent() ? advisor.get().getUsername() : "Unknown";
            }
            dto.setServiceAdvisor(advisorName);
            dto.setServiceDone(booking.getServices());
            dto.setCompletedDate(booking.getUpdatedAt());
            dto.setStatus(booking.getStatus());
            dto.setCustomerEmail(booking.getCustomerEmail());
            Optional<BillOfMaterial> bomOptional = billOfMaterialRepository.findByBookingId(booking.getId());
            boolean hasBom = bomOptional.isPresent();
            if (hasBom) {
                logger.info("BOM found for booking ID: {}. BOM details: customerName={}, advisorName={}, total={}",
                        booking.getId(), bomOptional.get().getCustomerName(), bomOptional.get().getAdvisorName(), bomOptional.get().getTotal());
            } else {
                logger.warn("No BOM found for booking ID: {}. Expected BOM to exist in bill_of_material table.", booking.getId());
            }
            dto.setHasBom(hasBom);
            return dto;
        }).collect(Collectors.toList());
        dashboardData.setVehiclesCompleted(vehiclesCompleted);
        dashboardData.setCompletedCount(vehiclesCompleted.size());

        
        List<AdvisorRequestDTO> advisorRequests = inProgressBookings.stream()
            .filter(booking -> booking.getStatus().equals("IN_PROGRESS"))
            .map(booking -> {
                AdvisorRequestDTO dto = new AdvisorRequestDTO();
                dto.setId(booking.getId());
                Optional<BookingAdvisorMapping> mapping = bookingAdvisorMappingRepository.findByBookingId(booking.getId());
                String advisorName = "Unknown";
                if (mapping.isPresent()) {
                    Optional<Advisor> advisor = advisorRepository.findById(mapping.get().getAdvisorId());
                    advisorName = advisor.isPresent() ? advisor.get().getUsername() : "Unknown";
                }
                dto.setAdvisorName(advisorName);
                dto.setRequestedService(booking.getServices());
                dto.setCustomerName(booking.getCustomerName());
                dto.setVehicleType(booking.getVehicleType());
                dto.setVehicleModel(booking.getVehicleModel());
                dto.setServiceCenter(booking.getServiceCenter());
                dto.setStatus("Pending");
                return dto;
            }).collect(Collectors.toList());
        dashboardData.setAdvisorRequests(advisorRequests);
        dashboardData.setAdvisorRequestsCount(advisorRequests.size());

        dashboardData.setProfileName("Admin");
        return dashboardData;
    }

    @Transactional
    public BookingRequest markBookingCompleted(Long bookingId, BillOfMaterialDTO bomDTO) {
        BookingRequest booking = repository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));
        if (!"COMPLETED".equalsIgnoreCase(booking.getStatus())) {
            booking.setStatus("COMPLETED");
            booking.setUpdatedAt(LocalDateTime.now());
            booking = repository.save(booking);

            try {
                bomDTO.setBookingId(bookingId);
                if (bomDTO.getCustomerName() == null) {
                    bomDTO.setCustomerName(booking.getCustomerName());
                }

                if (bomDTO.getAdvisorName() == null) {
                    String advisorName = "Unknown";
                    Optional<BookingAdvisorMapping> mapping = bookingAdvisorMappingRepository.findByBookingId(bookingId);
                    if (mapping.isPresent()) {
                        Optional<Advisor> advisor = advisorRepository.findById(mapping.get().getAdvisorId());
                        advisorName = advisor.map(Advisor::getUsername).orElse("Unknown");
                    }
                    bomDTO.setAdvisorName(advisorName);
                }

                if (bomDTO.getServiceName() == null) {
                    bomDTO.setServiceName(booking.getServices());
                }

                if (bomDTO.getTotal() == null && bomDTO.getMaterials() != null) {
                    Double total = bomDTO.getMaterials().stream()
                            .mapToDouble(m -> m.getPrice() * m.getQuantity())
                            .sum();
                    bomDTO.setTotal(total);
                }

                billOfMaterialService.saveBillOfMaterial(bomDTO);
                logger.info("Created BillOfMaterial for booking ID: {}", bookingId);
            } catch (Exception e) {
                logger.error("Error creating BillOfMaterial for booking ID {}: {}", bookingId, e.getMessage(), e);
                throw new RuntimeException("Failed to save Bill of Material", e);
            }
        }
        return booking;
    }

    @Transactional
    public BookingRequest markBookingCompleted(Long bookingId) {
        BillOfMaterialDTO bomDTO = new BillOfMaterialDTO();
        return markBookingCompleted(bookingId, bomDTO);
    }

    public Map<String, Object> getReceiptData(Long bookingId) {
        BookingRequest booking = repository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));
        if (!"COMPLETED".equalsIgnoreCase(booking.getStatus())) {
            throw new IllegalStateException("Receipt available only for completed bookings");
        }

        Optional<BillOfMaterial> bomOptional = billOfMaterialRepository.findByBookingId(bookingId);
        if (!bomOptional.isPresent()) {
            throw new RuntimeException("Bill of Material not found for booking ID: " + bookingId);
        }
        BillOfMaterial bom = bomOptional.get();

        Map<String, Object> receiptData = new HashMap<>();
        receiptData.put("customerName", bom.getCustomerName());
        receiptData.put("advisorName", bom.getAdvisorName());
        receiptData.put("serviceName", bom.getServiceName());
        receiptData.put("total", bom.getTotal());

        List<Map<String, Object>> materials = new ArrayList<>();
        try {
            if (bom.getMaterials() != null && !bom.getMaterials().isEmpty()) {
                materials = objectMapper.readValue(bom.getMaterials(), new TypeReference<List<Map<String, Object>>>() {});
            }
        } catch (JsonProcessingException e) {
            logger.error("Error parsing materials JSON for booking ID {}: {}", bookingId, e.getMessage(), e);
            throw new RuntimeException("Error parsing materials data for booking ID: " + bookingId);
        }
        receiptData.put("materials", materials);

        return receiptData;
    }

    @Transactional
    public void sendBill(Long bookingId) {
        BookingRequest booking = repository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with id: " + bookingId));
        if (!"COMPLETED".equals(booking.getStatus())) {
            throw new IllegalStateException("Booking must be completed to send bill");
        }

        BillOfMaterial bom = billOfMaterialRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Bill of Material not found for booking: " + bookingId));

        List<BillOfMaterialDTO.Material> materials = new ArrayList<>();
        try {
            if (bom.getMaterials() != null && !bom.getMaterials().isEmpty()) {
                materials = objectMapper.readValue(bom.getMaterials(), new TypeReference<List<BillOfMaterialDTO.Material>>() {});
            }
        } catch (JsonProcessingException e) {
            logger.error("Error parsing materials JSON for BOM ID {}: {}", bom.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to parse materials for bill email", e);
        }

        try {
            emailService.sendBillEmail(
                    booking.getCustomerEmail(),
                    booking.getCustomerName(),
                    bookingId,
                    bom.getCustomerName(),
                    bom.getAdvisorName(),
                    bom.getServiceName(),
                    materials,
                    bom.getTotal(),
                    bom.getServiceCharges()
            );
        } catch (MessagingException e) {
            logger.error("Error sending bill email for booking ID {}: {}", bookingId, e.getMessage(), e);
            throw new RuntimeException("Failed to send bill email", e);
        }

        booking.setStatus("COMPLETED_PENDING_PAYMENT");
        booking.setUpdatedAt(LocalDateTime.now());
        repository.save(booking);
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
        logger.info("Mapping {} bookings to DTOs", bookings.size());
        List<BookingResponseDTO> dtos = bookings.stream().map(booking -> {
            String vehicleTypeFormatted = booking.getVehicleTypeId() != null ? booking.getVehicleTypeId() + " (Unknown)" : "Unknown";
            String vehicleModelFormatted = booking.getVehicleModelId() != null ? booking.getVehicleModelId() + " (Unknown)" : "Unknown";
            String serviceCenterFormatted = booking.getServiceCenterId() != null ? booking.getServiceCenterId() + " (Unknown)" : "Unknown";

            try {
                if (booking.getVehicleTypeId() != null) {
                    Optional<VehicleType> vehicleType = vehicleTypeRepository.findById(booking.getVehicleTypeId());
                    if (vehicleType.isPresent()) {
                        vehicleTypeFormatted = vehicleType.get().getName();
                    }
                }

                if (booking.getVehicleModelId() != null) {
                    Optional<VehicleModel> vehicleModel = vehicleModelRepository.findById(booking.getVehicleModelId());
                    if (vehicleModel.isPresent()) {
                        vehicleModelFormatted = vehicleModel.get().getModelName();
                    }
                }

                if (booking.getServiceCenterId() != null) {
                    Optional<ServiceCenter> serviceCenter = serviceCenterRepository.findById(booking.getServiceCenterId());
                    if (serviceCenter.isPresent()) {
                        serviceCenterFormatted = serviceCenter.get().getCenterName() + " (" + serviceCenter.get().getCityName() + ")";
                    }
                }
            } catch (Exception e) {
                logger.error("Error fetching details for booking ID {}: {}", booking.getId(), e.getMessage(), e);
            }

            BookingResponseDTO dto = new BookingResponseDTO(booking, serviceCenterFormatted, vehicleTypeFormatted, vehicleModelFormatted);
            logger.debug("Mapped booking ID {} to DTO: {}", booking.getId(), dto);
            return dto;
        }).collect(Collectors.toList());
        logger.info("Mapped to {} DTOs", dtos.size());
        return dtos;
    }

   

    private String generateBillEmailContent(BillOfMaterial bom, String paymentLink) {
        StringBuilder content = new StringBuilder();
        content.append("<h2>Service Bill</h2>");
        content.append("<p>Customer Name: ").append(bom.getCustomerName()).append("</p>");
        content.append("<p>Service: ").append(bom.getServiceName()).append("</p>");
        content.append("<table border='1'><tr><th>Material</th><th>Quantity</th><th>Unit Price</th><th>Total</th></tr>");

        List<BillOfMaterialDTO.Material> materials;
        try {
            if (bom.getMaterials() != null && !bom.getMaterials().isEmpty()) {
                materials = objectMapper.readValue(bom.getMaterials(), new TypeReference<List<BillOfMaterialDTO.Material>>() {});
                for (BillOfMaterialDTO.Material material : materials) {
                    content.append("<tr>")
                           .append("<td>").append(material.getMaterialName()).append("</td>")
                           .append("<td>").append(material.getQuantity()).append("</td>")
                           .append("<td>₹").append(material.getPrice()).append("</td>")
                           .append("<td>₹").append(material.getPrice() * material.getQuantity()).append("</td>")
                           .append("</tr>");
                }
            }
        } catch (JsonProcessingException e) {
            logger.error("Error parsing materials JSON for BOM ID {}: {}", bom.getId(), e.getMessage(), e);
            throw new RuntimeException("Failed to parse materials for bill email", e);
        }

        content.append("</table>");
        content.append("<p><strong>Total: ₹").append(bom.getTotal()).append("</strong></p>");
        content.append("<p><a href='").append(paymentLink).append("'><button>Pay Now</button></a></p>");
        return content.toString();
    }

    public PaymentResponse createPayment(Long bookingId, PaymentRequest request) throws RazorpayException {
        BookingRequest booking = repository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with id: " + bookingId));
        if (!"COMPLETED_PENDING_PAYMENT".equals(booking.getStatus())) {
            throw new IllegalStateException("Booking must be in COMPLETED_PENDING_PAYMENT status");
        }

        RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", request.getAmount());
        orderRequest.put("currency", request.getCurrency());
        orderRequest.put("receipt", "booking_" + bookingId);

        Order order = razorpay.orders.create(orderRequest);
        PaymentResponse response = new PaymentResponse();
        response.setOrderId(order.get("id"));
        response.setAmount(request.getAmount());
        response.setCurrency(request.getCurrency());
        return response;
    }

    @Transactional
    public void verifyPayment(PaymentVerificationRequest request) throws RazorpayException {
        BookingRequest booking = repository.findById(Long.valueOf(request.getBookingId()))
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with id: " + request.getBookingId()));

        JSONObject attributes = new JSONObject();
        attributes.put("razorpay_order_id", request.getRazorpayOrderId());
        attributes.put("razorpay_payment_id", request.getRazorpayPaymentId());
        attributes.put("razorpay_signature", request.getRazorpaySignature());

        try {
            Utils.verifyPaymentSignature(attributes, razorpayKeySecret);
            logger.info("Payment signature verified successfully for booking ID: {}", request.getBookingId());
        } catch (RazorpayException e) {
            logger.error("Payment signature verification failed for booking ID {}: {}", request.getBookingId(), e.getMessage(), e);
            throw new RazorpayException("Invalid payment signature: " + e.getMessage());
        }

        booking.setStatus("COMPLETED_PAID");
        booking.setTransactionId(request.getRazorpayPaymentId());
        booking.setUpdatedAt(LocalDateTime.now());
        repository.save(booking);
    }

    public BillOfMaterialDTO getBillOfMaterials(Long bookingId, Long customerId) {
        logger.info("Fetching BOM for bookingId: {}, customerId: {}", bookingId, customerId);

        BookingRequest booking = repository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with id: " + bookingId));


        if (customerId != 0 && !booking.getCustomerId().equals(customerId)) {
            logger.warn("Customer ID {} does not match booking ID {} owner", customerId, bookingId);
            throw new IllegalArgumentException("Booking does not belong to customer: " + customerId);
        }

        if (!List.of("COMPLETED", "COMPLETED_PENDING_PAYMENT", "COMPLETED_PAID").contains(booking.getStatus())) {
            logger.warn("Booking ID {} is not in COMPLETED, COMPLETED_PENDING_PAYMENT, or COMPLETED_PAID status, current status: {}", bookingId, booking.getStatus());
            throw new IllegalArgumentException("Booking must be in COMPLETED, COMPLETED_PENDING_PAYMENT, or COMPLETED_PAID status");
        }

        BillOfMaterial bom = billOfMaterialRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Bill of Material not found for booking ID: " + bookingId));

        BillOfMaterialDTO bomDTO = new BillOfMaterialDTO();
        bomDTO.setBookingId(bookingId);
        bomDTO.setCustomerName(bom.getCustomerName());
        bomDTO.setAdvisorName(bom.getAdvisorName());
        bomDTO.setServiceName(bom.getServiceName());
        bomDTO.setTotal(bom.getTotal());

        List<BillOfMaterialDTO.Material> materials = new ArrayList<>();
        try {
            if (bom.getMaterials() != null && !bom.getMaterials().isEmpty()) {
                materials = objectMapper.readValue(bom.getMaterials(), new TypeReference<List<BillOfMaterialDTO.Material>>() {});
            }
        } catch (JsonProcessingException e) {
            logger.error("Error parsing materials JSON for booking ID {}: {}", bookingId, e.getMessage(), e);
            throw new RuntimeException("Error parsing materials data for booking ID: " + bookingId);
        }
        bomDTO.setMaterials(materials);

        logger.info("Successfully fetched BOM for bookingId: {}", bookingId);
        return bomDTO;
    }
  
    public List<ServiceHistory> getAllServiceHistory() {
        logger.info("Fetching all service histories");
        List<BookingRequest> bookings = repository.findByStatus("COMPLETED_PAID");
        logger.info("Found {} completed bookings", bookings.size());

        List<ServiceHistory> serviceHistory = new ArrayList<>();

        for (BookingRequest booking : bookings) {
            if (booking.getTransactionId() == null) {
                continue;
            }

            String vehicleTypeName = vehicleTypeRepository.findById(booking.getVehicleTypeId())
                    .map(VehicleType::getName).orElse("Unknown");
            String vehicleModelName = vehicleModelRepository.findById(booking.getVehicleModelId())
                    .map(VehicleModel::getModelName).orElse("Unknown");
            String serviceCenterName = serviceCenterRepository.findById(booking.getServiceCenterId())
                    .map(ServiceCenter::getCenterName).orElse("Unknown");
            Double cost = billOfMaterialRepository.findByBookingId(booking.getId())
                    .map(BillOfMaterial::getTotal).orElse(0.0);
            String customerName = customerRepository.findById(booking.getCustomerId())
                    .map(Customer::getUsername).orElse("Unknown");

            ServiceHistory history = new ServiceHistory();
            history.setId(booking.getId());
            history.setDate(booking.getUpdatedAt().toString());
            history.setServiceCenterName(serviceCenterName);
            history.setVehicleTypeName(vehicleTypeName);
            history.setVehicleModelName(vehicleModelName);
            history.setWorkDone(booking.getServices());
            history.setCost(cost);
            history.setStatus("Completed");
            history.setTransactionId(booking.getTransactionId());
            history.setCustomerName(customerName);
            serviceHistory.add(history);
        }

        logger.info("Returning {} service history entries", serviceHistory.size());
        return serviceHistory;
    }
    
    public List<BookingResponseDTO> getCompletedBookingsForAdvisor(Long advisorId) {
        logger.info("Fetching completed bookings for advisorId: {}", advisorId);
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

        List<BookingRequest> completedBookings = repository.findByIdInAndStatusIn(
            bookingIds, Arrays.asList("COMPLETED", "COMPLETED_PENDING_PAYMENT", "COMPLETED_PAID"));
        if (completedBookings == null || completedBookings.isEmpty()) {
            logger.debug("No completed bookings found for advisorId: {}", advisorId);
            return new ArrayList<>();
        }

        return mapToBookingResponseDTOs(completedBookings);
    }
    
    @Transactional
    public BillOfMaterialDTO updateBillWithServiceCharges(Long bookingId, Long customerId, Double serviceCharges) {
        logger.info("Updating BOM with service charges for bookingId: {}, customerId: {}", bookingId, customerId);

        BookingRequest booking = repository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with id: " + bookingId));

        // Skip customerId check if customerId is 0 (indicating admin/advisor access)
        if (customerId != 0 && !booking.getCustomerId().equals(customerId)) {
            logger.warn("Customer ID {} does not match booking ID {} owner", customerId, bookingId);
            throw new IllegalArgumentException("Booking does not belong to customer: " + customerId);
        }

        if (!List.of("COMPLETED", "COMPLETED_PENDING_PAYMENT").contains(booking.getStatus())) {
            logger.warn("Booking ID {} is not in COMPLETED or COMPLETED_PENDING_PAYMENT status, current status: {}", bookingId, booking.getStatus());
            throw new IllegalArgumentException("Booking must be in COMPLETED or COMPLETED_PENDING_PAYMENT status");
        }

        BillOfMaterial bom = billOfMaterialRepository.findByBookingId(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Bill of Material not found for booking ID: " + bookingId));

        // Update service charges and recalculate total
        bom.setServiceCharges(serviceCharges != null ? serviceCharges : 0.0);
        Double materialTotal = 0.0;
        try {
            if (bom.getMaterials() != null && !bom.getMaterials().isEmpty()) {
                List<BillOfMaterialDTO.Material> materials = objectMapper.readValue(bom.getMaterials(), new TypeReference<List<BillOfMaterialDTO.Material>>() {});
                materialTotal = materials.stream().mapToDouble(m -> m.getPrice() * m.getQuantity()).sum();
            }
        } catch (JsonProcessingException e) {
            logger.error("Error parsing materials JSON for booking ID {}: {}", bookingId, e.getMessage(), e);
            throw new RuntimeException("Error parsing materials data for booking ID: " + bookingId);
        }
        bom.setTotal(materialTotal + bom.getServiceCharges());
        billOfMaterialRepository.save(bom);

        // Convert to DTO
        BillOfMaterialDTO bomDTO = new BillOfMaterialDTO();
        bomDTO.setBookingId(bookingId);
        bomDTO.setCustomerName(bom.getCustomerName());
        bomDTO.setAdvisorName(bom.getAdvisorName());
        bomDTO.setServiceName(bom.getServiceName());
        bomDTO.setTotal(bom.getTotal());
        bomDTO.setServiceCharges(bom.getServiceCharges());
        try {
            List<BillOfMaterialDTO.Material> materials = bom.getMaterials() != null && !bom.getMaterials().isEmpty()
                    ? objectMapper.readValue(bom.getMaterials(), new TypeReference<List<BillOfMaterialDTO.Material>>() {})
                    : new ArrayList<>();
            bomDTO.setMaterials(materials);
        } catch (JsonProcessingException e) {
            logger.error("Error parsing materials JSON for booking ID {}: {}", bookingId, e.getMessage(), e);
            throw new RuntimeException("Error parsing materials data for booking ID: " + bookingId);
        }

        logger.info("Successfully updated BOM with service charges for bookingId: {}", bookingId);
        return bomDTO;
    }
    
}