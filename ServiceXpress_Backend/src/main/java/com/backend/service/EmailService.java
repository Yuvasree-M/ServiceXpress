package com.backend.service;

import com.backend.dto.BillOfMaterialDTO;
import com.backend.model.BookingRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.util.List;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    public void sendBookingConfirmationEmail(String to, String customerName, BookingRequest booking,
                                             String vehicleTypeId, String vehicleTypeName,
                                             String vehicleModelId, String vehicleModelName,
                                             String serviceCenterId, String serviceCenterName) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("Booking Confirmation - " + booking.getId());
        helper.setFrom("noreply@yourdomain.com");

        String htmlContent = "<h3>Dear " + customerName + ",</h3>" +
                "<p>Your booking has been successfully created with the following details:</p>" +
                "<ul>" +
                "<li><strong>Booking ID:</strong> " + booking.getId() + "</li>" +
                "<li><strong>Vehicle Type:</strong> " + vehicleTypeName + " (ID: " + vehicleTypeId + ")</li>" +
                "<li><strong>Vehicle Model:</strong> " + vehicleModelName + " (ID: " + vehicleModelId + ")</li>" +
                "<li><strong>Service Center:</strong> " + serviceCenterName + " (ID: " + serviceCenterId + ")</li>" +
                "<li><strong>Services:</strong> " + booking.getServices() + "</li>" +
                "<li><strong>Requested Date:</strong> " + booking.getRequestedDate() + "</li>" +
                "</ul>" +
                "<p>Thank you for choosing our service!</p>";

        helper.setText(htmlContent, true);
        mailSender.send(message);
        logger.info("Booking confirmation email sent to: {}", to);
    }

    public void sendBillEmail(String to, String customerName, Long bookingId, String customerNameInBom, 
                             String advisorName, String serviceName, List<BillOfMaterialDTO.Material> materials, 
                             Double total) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject("Bill for Booking ID: " + bookingId);
        helper.setFrom("noreply@yourdomain.com");

        StringBuilder materialsHtml = new StringBuilder();
        materialsHtml.append("<table border='1' style='border-collapse: collapse; width: 100%;'>" +
                "<tr><th>Material</th><th>Quantity</th><th>Unit Price</th><th>Total Price</th></tr>");
        for (BillOfMaterialDTO.Material material : materials) {
            double totalPrice = material.getQuantity() * material.getPrice();
            materialsHtml.append("<tr>" +
                    "<td>").append(material.getMaterialName()).append("</td>" +
                    "<td>").append(material.getQuantity()).append("</td>" +
                    "<td>$").append(String.format("%.2f", material.getPrice())).append("</td>" +
                    "<td>$").append(String.format("%.2f", totalPrice)).append("</td>" +
                    "</tr>");
        }
        materialsHtml.append("</table>");

        String htmlContent = "<h3>Dear " + customerName + ",</h3>" +
                "<p>Below is the bill for your recent service (Booking ID: " + bookingId + "):</p>" +
                "<ul>" +
                "<li><strong>Customer Name:</strong> " + customerNameInBom + "</li>" +
                "<li><strong>Advisor Name:</strong> " + advisorName + "</li>" +
                "<li><strong>Service Name:</strong> " + serviceName + "</li>" +
                "</ul>" +
                "<h4>Materials Used:</h4>" +
                materialsHtml.toString() +
                "<p><strong>Total Amount:</strong> $" + String.format("%.2f", total) + "</p>" +
                "<p>Please make the payment at your earliest convenience.</p>" +
                "<p>Thank you for choosing our service!</p>";

        helper.setText(htmlContent, true);
        mailSender.send(message);
        logger.info("Bill email sent to: {}", to);
    }
}