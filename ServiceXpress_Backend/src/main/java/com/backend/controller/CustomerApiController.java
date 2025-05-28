package com.backend.controller;

import com.backend.dto.ContactRequestDTO;
import com.backend.dto.CustomerDashboardDTO;
import com.backend.dto.CustomerProfileDTO;
import com.backend.dto.ReviewDTO;
import com.backend.model.Customer;
import com.backend.repository.CustomerRepository;
import com.backend.service.BookingRequestService;
import com.backend.service.ReviewService;
import com.backend.service.SupportTicketService;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
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
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
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
    
    @Autowired
    private ReviewService reviewService;

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
                logger.error("Invalid customer ID for receipt ID {}: customerId must be a positive number", id);
                return ResponseEntity.badRequest().build();
            }
            Map<String, Object> receiptData = bookingRequestService.getReceiptData(id);

            Long bookingCustomerId;
            try {
                bookingCustomerId = Long.parseLong(receiptData.get("customerId").toString());
            } catch (NumberFormatException e) {
                logger.error("Invalid customer ID format in receipt data for booking ID {}: {}", id, e.getMessage());
                return ResponseEntity.badRequest().build();
            }
            if (!customerId.equals(bookingCustomerId)) {
                logger.error("Unauthorized access attempt for receipt ID {} by customer ID {}", id, customerId);
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            // Fetch customer details for the invoice
            Optional<Customer> customerOpt = customerRepository.findById(customerId);
            if (!customerOpt.isPresent()) {
                logger.error("Customer not found for ID: {}", customerId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            Customer customer = customerOpt.get();
            String customerPhone = customer.getPhoneNumber() != null ? customer.getPhoneNumber() : "Not provided";

            // Generate PDF using iText
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Header
            Paragraph header = new Paragraph("ServiceXpress")
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(header);

            Paragraph subHeader = new Paragraph("123 Service Lane, Tech City, TC 12345\nPhone: (123) 456-7890 | Email: support@servicexpress.com")
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(subHeader);

            String currentDate = LocalDateTime.now(ZoneId.of("Asia/Kolkata"))
                    .format(DateTimeFormatter.ofPattern("dd MMMM yyyy"));
            Paragraph invoiceTitle = new Paragraph("Invoice")
                    .setFontSize(16)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(10);
            document.add(invoiceTitle);

            Paragraph invoiceDetails = new Paragraph(String.format("Invoice No: #%d | Date: %s", id, currentDate))
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(invoiceDetails);

            // Billed To and Invoice Details
            Table infoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginTop(20);
            infoTable.addCell(new Cell().add(new Paragraph("Billed To:")
                    .setBold()));
            infoTable.addCell(new Cell().add(new Paragraph("Invoice Details:")
                    .setBold()));
            infoTable.addCell(new Cell().add(new Paragraph(String.valueOf(receiptData.get("customerName")))));
            infoTable.addCell(new Cell().add(new Paragraph(String.format("Booking ID: %s", receiptData.get("bookingId")))));
            infoTable.addCell(new Cell().add(new Paragraph(String.format("Email: %s", receiptData.get("customerEmail")))));
            infoTable.addCell(new Cell().add(new Paragraph(String.format("Transaction ID: %s", receiptData.get("transactionId") != null ? receiptData.get("transactionId") : "N/A"))));
            infoTable.addCell(new Cell().add(new Paragraph(String.format("Phone: %s", customerPhone))));
            infoTable.addCell(new Cell().add(new Paragraph(String.format("Completion Date: %s", receiptData.get("completionDate")))));
            infoTable.addCell(new Cell().add(new Paragraph("")));
            infoTable.addCell(new Cell().add(new Paragraph(String.format("Service Center: %s", receiptData.get("serviceCenterName")))));
            document.add(infoTable);

            // Services Table
            Table serviceTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1, 1, 1}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginTop(20);
            serviceTable.addHeaderCell(new Cell().add(new Paragraph("Vehicle").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            serviceTable.addHeaderCell(new Cell().add(new Paragraph("Registration").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            serviceTable.addHeaderCell(new Cell().add(new Paragraph("Services").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            serviceTable.addHeaderCell(new Cell().add(new Paragraph("Cost").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            serviceTable.addHeaderCell(new Cell().add(new Paragraph("Advisor").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            serviceTable.addCell(new Cell().add(new Paragraph(String.format("%s - %s", receiptData.get("vehicleTypeName"), receiptData.get("vehicleModelName")))));
            serviceTable.addCell(new Cell().add(new Paragraph(String.valueOf(receiptData.get("vehicleRegistrationNumber")))));
            serviceTable.addCell(new Cell().add(new Paragraph(String.valueOf(receiptData.get("services")))));
            serviceTable.addCell(new Cell().add(new Paragraph(String.format("₹%s", receiptData.get("cost")))));
            serviceTable.addCell(new Cell().add(new Paragraph(String.valueOf(receiptData.get("advisorName") != null ? receiptData.get("advisorName") : "N/A"))));
            document.add(serviceTable);

            // Materials Table
            document.add(new Paragraph("Materials Used:").setBold().setMarginTop(20));
            Table materialsTable = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1, 1}))
                    .setWidth(UnitValue.createPercentValue(100));
            materialsTable.addHeaderCell(new Cell().add(new Paragraph("Material").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            materialsTable.addHeaderCell(new Cell().add(new Paragraph("Quantity").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            materialsTable.addHeaderCell(new Cell().add(new Paragraph("Unit Price").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));
            materialsTable.addHeaderCell(new Cell().add(new Paragraph("Total").setBold()).setBackgroundColor(ColorConstants.LIGHT_GRAY));

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> materials = (List<Map<String, Object>>) receiptData.get("materials");
            if (materials != null && !materials.isEmpty()) {
                for (Map<String, Object> material : materials) {
                    String materialName = material.get("materialName") != null ? String.valueOf(material.get("materialName")) : "Unknown";
                    String quantity = material.get("quantity") != null ? String.valueOf(material.get("quantity")) : "0";
                    String price = material.get("price") != null ? String.valueOf(material.get("price")) : "0";
                    double total;
                    try {
                        double priceValue = Double.parseDouble(price);
                        double quantityValue = Double.parseDouble(quantity);
                        total = priceValue * quantityValue;
                    } catch (NumberFormatException e) {
                        logger.warn("Invalid price or quantity for material in booking ID {}: price={}, quantity={}", id, price, quantity);
                        total = 0.0;
                    }
                    materialsTable.addCell(new Cell().add(new Paragraph(materialName)));
                    materialsTable.addCell(new Cell().add(new Paragraph(quantity)));
                    materialsTable.addCell(new Cell().add(new Paragraph(String.format("₹%s", price))));
                    materialsTable.addCell(new Cell().add(new Paragraph(String.format("₹%.2f", total))));
                }
            } else {
                materialsTable.addCell(new Cell().add(new Paragraph("No materials used")));
                materialsTable.addCell(new Cell().add(new Paragraph("-")));
                materialsTable.addCell(new Cell().add(new Paragraph("-")));
                materialsTable.addCell(new Cell().add(new Paragraph("-")));
            }
            document.add(materialsTable);

            // Total Cost
            Table totalTable = new Table(UnitValue.createPercentArray(new float[]{1, 1}))
                    .setWidth(UnitValue.createPercentValue(100))
                    .setMarginTop(20);
            totalTable.addCell(new Cell().add(new Paragraph("")));
            totalTable.addCell(new Cell().add(new Paragraph(String.format("Total Cost: ₹%s", receiptData.get("cost")))
                    .setBold()));
            document.add(totalTable);

            // Footer
            Paragraph footer = new Paragraph("Thank You for Choosing ServiceXpress!")
                    .setFontSize(14)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginTop(30);
            document.add(footer);

            Paragraph footerSub = new Paragraph("We value your trust in us. For any queries, feel free to contact us at support@servicexpress.com.\nDrive Safe, Service Smart!")
                    .setFontSize(10)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setItalic();
            document.add(footerSub);

            document.close();

            byte[] pdfBytes = baos.toByteArray();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "invoice_" + id + ".pdf");
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(pdfBytes);
        } catch (IllegalStateException e) {
            logger.error("Invalid request for receipt ID {}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException e) {
            logger.error("Booking not found for receipt ID {}: {}", id, e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            logger.error("Error generating receipt for ID {}: {}", id, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
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
    
    @PostMapping("/reviews")
    public ResponseEntity<String> submitReview(@RequestBody ReviewDTO reviewDTO) {
        logger.debug("Received review submission: {}", reviewDTO);
        try {
            reviewService.createReview(reviewDTO);
            logger.info("Review submitted successfully for booking ID: {}", reviewDTO.getBookingId());
            return ResponseEntity.ok("Review submitted successfully");
        } catch (IllegalArgumentException e) {
            logger.error("Invalid review submission: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            logger.error("Invalid state for review submission: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error processing review: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to submit review: " + e.getMessage());
        }
    }

    }