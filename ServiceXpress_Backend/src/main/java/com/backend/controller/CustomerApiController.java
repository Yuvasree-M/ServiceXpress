package com.backend.controller;

import com.backend.dto.CustomerDashboardDTO;
import com.backend.service.BookingRequestService;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.Map;

@RestController
@CrossOrigin(origins = "${app.cors.allowed-origins:http://localhost:8082}")
@RequestMapping("/api")
public class CustomerApiController {

    @Autowired
    private BookingRequestService bookingRequestService;

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

            // Verify customer ownership
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

            // Generate PDF
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("Service Receipt").setBold().setFontSize(16));

            // Create table for receipt details
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
}