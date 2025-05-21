package com.backend.controller;

import com.backend.dto.ContactRequestDTO;
import com.backend.dto.CustomerDashboardDTO;
import com.backend.dto.CustomerProfileDTO;
import com.backend.model.Customer;
import com.backend.repository.CustomerRepository;
import com.backend.service.BookingRequestService;
import com.backend.service.SupportTicketService;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;

import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "${app.cors.allowed-origins:http://localhost:8082}")
@RequestMapping("/api")
public class CustomerApiController {

    private static final Logger logger = LoggerFactory.getLogger(CustomerApiController.class);

    @Autowired
    private BookingRequestService bookingRequestService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private SupportTicketService supportTicketService;

    @GetMapping("/customer/dashboard")
    public ResponseEntity<CustomerDashboardDTO> getDashboardData(@RequestParam("customerId") Long customerId) {
        try {
            if (customerId == null || customerId <= 0) {
                System.err.println("Invalid customer ID for dashboard data");
                return ResponseEntity.badRequest().build();
            }
            CustomerDashboardDTO data = bookingRequestService.getDashboardData(customerId);
            return ResponseEntity.ok(data);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid request for dashboard data: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("Error fetching dashboard data: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/bookings/receipt/{id}")
    public ResponseEntity<byte[]> downloadReceipt(@PathVariable Long id, @RequestParam("customerId") Long customerId) {
        try {
            if (customerId == null || customerId <= 0) {
                System.err.println("Invalid customer ID for receipt ID " + id);
                return ResponseEntity.badRequest().build();
            }
            Map<String, Object> receiptData = bookingRequestService.getReceiptData(id);

            Long bookingCustomerId;
            try {
                bookingCustomerId = Long.parseLong(receiptData.get("customerId").toString());
            } catch (NumberFormatException e) {
                System.err.println("Invalid customer ID format in receipt data for booking ID " + id + ": " + e.getMessage());
                return ResponseEntity.badRequest().build();
            }
            if (!customerId.equals(bookingCustomerId)) {
                System.err.println("Unauthorized access attempt for receipt ID " + id + " by customer ID " + customerId);
                return ResponseEntity.status(403).build();
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Service Receipt").setBold().setFontSize(16));

            Table table = new Table(new float[]{1, 3});
            table.addCell(new Cell().add(new Paragraph("Field")));
            table.addCell(new Cell().add(new Paragraph("Value")));
            table.addCell(new Cell().add(new Paragraph("Booking ID")));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(receiptData.get("bookingId")))));
            table.addCell(new Cell().add(new Paragraph("Transaction ID")));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(receiptData.get("transactionId")))));
            table.addCell(new Cell().add(new Paragraph("Customer")));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(receiptData.get("customerName")))));
            table.addCell(new Cell().add(new Paragraph("Email")));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(receiptData.get("customerEmail")))));
            table.addCell(new Cell().add(new Paragraph("Service Center")));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(receiptData.get("serviceCenterName")))));
            table.addCell(new Cell().add(new Paragraph("Vehicle")));
            table.addCell(new Cell().add(new Paragraph(receiptData.get("vehicleTypeName") + " - " + receiptData.get("vehicleModelName"))));
            table.addCell(new Cell().add(new Paragraph("Registration")));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(receiptData.get("vehicleRegistrationNumber")))));
            table.addCell(new Cell().add(new Paragraph("Services")));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(receiptData.get("services")))));
            table.addCell(new Cell().add(new Paragraph("Total Cost")));
            table.addCell(new Cell().add(new Paragraph("$" + receiptData.get("cost"))));
            table.addCell(new Cell().add(new Paragraph("Completion Date")));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(receiptData.get("completionDate")))));

            document.add(table);
            document.close();

            byte[] pdfBytes = baos.toByteArray();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "receipt_" + id + ".pdf");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (IllegalStateException e) {
            System.err.println("Invalid request for receipt ID " + id + ": " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            System.err.println("Booking not found for receipt ID " + id + ": " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.err.println("Error generating receipt for ID " + id + ": " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/customer/profile")
    public ResponseEntity<CustomerProfileDTO> getCustomerProfile(@RequestParam Long customerId) {
        logger.debug("Fetching profile for customer ID: {}", customerId);
        try {
            Optional<Customer> customerOpt = customerRepository.findById(customerId);
            if (!customerOpt.isPresent()) {
                logger.warn("Customer not found for ID: {}", customerId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new CustomerProfileDTO(null, null, null, null));
            }

            Customer customer = customerOpt.get();
            CustomerProfileDTO customerProfileDTO = new CustomerProfileDTO();
            customerProfileDTO.setUsername(customer.getUsername());
            customerProfileDTO.setEmail(customer.getEmail());
            customerProfileDTO.setPhoneNumber(customer.getPhoneNumber());
            customerProfileDTO.setRole(customer.getRole().toString());

            logger.info("Profile fetched successfully for customer ID: {}. Response: {}", customerId, customerProfileDTO);
            return ResponseEntity.ok(customerProfileDTO);
        } catch (Exception e) {
            logger.error("Unexpected error fetching customer profile for ID: {}. Exception: {}", customerId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new CustomerProfileDTO(null, null, null, null));
        }
    }

    @PostMapping("/customer/profile/update")
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public ResponseEntity<String> updateCustomerProfile(
            @RequestParam Long customerId,
            @RequestParam(value = "username", required = false) String username,
            @RequestParam(value = "email", required = false) String email) {
        logger.debug("Updating profile for customer ID: {}. Username: {}, Email: {}",
                customerId, username, email);
        try {
            Optional<Customer> customerOpt = customerRepository.findById(customerId);
            if (!customerOpt.isPresent()) {
                logger.warn("Customer not found for ID: {}", customerId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Customer not found");
            }

            Customer customer = customerOpt.get();

            // Validate required fields before update
            if (customer.getUsername() == null || customer.getEmail() == null ||
                customer.getPassword() == null || customer.getRole() == null) {
                throw new RuntimeException("Customer record has missing required fields");
            }

            boolean hasChanges = false;

            // Track if fields are provided
            boolean usernameProvided = username != null && !username.trim().isEmpty();
            boolean emailProvided = email != null && !email.trim().isEmpty();

            // Update username if provided and different
            if (usernameProvided) {
                if (username.length() < 3) {
                    throw new RuntimeException("Username must be at least 3 characters long");
                }
                Optional<Customer> existingCustomer = customerRepository.findByUsername(username);
                if (existingCustomer.isPresent() && !existingCustomer.get().getId().equals(customerId)) {
                    logger.warn("Username already in use: {}", username);
                    throw new RuntimeException("Username is already in use");
                }
                String currentUsername = customer.getUsername() != null ? customer.getUsername().trim() : "";
                if (!username.trim().equalsIgnoreCase(currentUsername)) {
                    customer.setUsername(username.trim());
                    hasChanges = true;
                }
            }

            // Update email if provided and different
            if (emailProvided) {
                if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                    throw new RuntimeException("Invalid email format");
                }
                Optional<Customer> existingCustomer = customerRepository.findByEmail(email);
                if (existingCustomer.isPresent() && !existingCustomer.get().getId().equals(customerId)) {
                    logger.warn("Email already in use: {}", email);
                    throw new RuntimeException("Email is already in use");
                }
                String currentEmail = customer.getEmail() != null ? customer.getEmail().trim() : "";
                if (!email.trim().equalsIgnoreCase(currentEmail)) {
                    customer.setEmail(email.trim());
                    hasChanges = true;
                }
            }

            // Check if any fields were provided
            if (!usernameProvided && !emailProvided) {
                logger.info("No fields provided for customer ID: {}", customerId);
                throw new RuntimeException("No fields provided to update");
            }

            // Check if there are actual changes
            if (!hasChanges) {
                logger.info("No changes detected for customer ID: {}", customerId);
                throw new RuntimeException("No changes provided to update");
            }

            // Save the updated customer
            Customer updatedCustomer = customerRepository.save(customer);

            CustomerProfileDTO customerProfileDTO = new CustomerProfileDTO();
            customerProfileDTO.setUsername(updatedCustomer.getUsername());
            customerProfileDTO.setEmail(updatedCustomer.getEmail());
            customerProfileDTO.setPhoneNumber(updatedCustomer.getPhoneNumber());
            customerProfileDTO.setRole(updatedCustomer.getRole().toString());

            logger.info("Profile updated successfully for customer ID: {}. Response: {}", customerId, customerProfileDTO);
            return ResponseEntity.ok(customerProfileDTO.toString());
        } catch (RuntimeException e) {
            logger.error("Error updating customer profile for ID: {}. Message: {}", customerId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error updating customer profile for ID: {}. Exception: {}", customerId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error: " + e.getMessage());
        }
    }
    @PostMapping("/contact")
    public ResponseEntity<String> submitContactForm(@RequestBody ContactRequestDTO request) {
        logger.debug("Received contact form submission: {}", request);
        try {
            // Validate request
            if (request.getName() == null || request.getName().trim().length() < 3) {
                throw new IllegalArgumentException("Name must be at least 3 characters long");
            }
            if (request.getEmail() == null || !request.getEmail().matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                throw new IllegalArgumentException("Invalid email format");
            }
            if (request.getSubject() == null || request.getSubject().trim().length() < 5) {
                throw new IllegalArgumentException("Subject must be at least 5 characters long");
            }
            if (request.getMessage() == null || request.getMessage().trim().length() < 10) {
                throw new IllegalArgumentException("Message must be at least 10 characters long");
            }

            // Create support ticket
            supportTicketService.createSupportTicket(request);

            logger.info("Support ticket created successfully for email: {}", request.getEmail());
            return ResponseEntity.ok("Support query submitted successfully");
        } catch (IllegalArgumentException e) {
            logger.error("Invalid contact form submission: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error processing contact form: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to submit support query: " + e.getMessage());
        }
    }

    }