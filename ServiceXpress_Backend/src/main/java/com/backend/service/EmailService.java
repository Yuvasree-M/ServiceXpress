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

    public void sendEmail(String to, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }
    public void sendBookingConfirmationEmail(String to, String customerName, BookingRequest booking,
                                            String vehicleTypeId, String vehicleTypeName,
                                            String vehicleModelId, String vehicleModelName,
                                            String serviceCenterId, String serviceCenterName) {
        String content = "<h2>Booking Confirmation</h2>" +
                        "<p>Dear " + customerName + ",</p>" +
                        "<p>Your booking has been successfully created with the following details:</p>" +
                        "<p><strong>Booking ID:</strong> " + booking.getId() + "</p>" +
                        "<p><strong>Vehicle:</strong> " + vehicleTypeName + " - " + vehicleModelName + "</p>" +
                        "<p><strong>Services:</strong> " + booking.getServices() + "</p>" +
                        "<p><strong>Service Center:</strong> " + serviceCenterName + "</p>" +
                        "<p><strong>Requested Date:</strong> " + booking.getRequestedDate() + "</p>" +
                        "<p>Thank you for choosing our service!</p>";
        sendEmail(to, "Booking Confirmation - ID: " + booking.getId(), content);
    }
    public void sendBillEmail(String to, String customerName, Long bookingId, String customerNameInBom,
            String advisorName, String serviceName, List<BillOfMaterialDTO.Material> materials,
            Double total, Double serviceCharges) throws MessagingException {
MimeMessage message = mailSender.createMimeMessage();
MimeMessageHelper helper = new MimeMessageHelper(message, true);

helper.setTo(to);
helper.setSubject("Bill for Booking ID: " + bookingId);
helper.setFrom("noreply@yourdomain.com");

StringBuilder materialsHtml = new StringBuilder();
materialsHtml.append("<table border='1' style='border-collapse: collapse; width: 100%;'>" +
"<tr><th>Material</th><th>Quantity</th><th>Unit Price</th><th>Total Price</th></tr>");
double materialsTotal = 0.0;
for (BillOfMaterialDTO.Material material : materials) {
double totalPrice = material.getQuantity() * material.getPrice();
materialsTotal += totalPrice;
materialsHtml.append("<tr>" +
  "<td>").append(material.getMaterialName()).append("</td>" +
  "<td>").append(material.getQuantity()).append("</td>" +
  "<td>₹").append(String.format("%.2f", material.getPrice())).append("</td>" +
  "<td>₹").append(String.format("%.2f", totalPrice)).append("</td>" +
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
"<p><strong>Materials Total:</strong> ₹" + String.format("%.2f", materialsTotal) + "</p>" +
"<p><strong>Service Charges:</strong> ₹" + String.format("%.2f", serviceCharges != null ? serviceCharges : 0.0) + "</p>" +
"<p><strong>Total Amount:</strong> ₹" + String.format("%.2f", total) + "</p>" +
"<p><a href='http://localhost:8082/customer/dashboard?bookingId=" + bookingId + "'>Click here to make payment</a></p>" +
"<p>Thank you for choosing our service!</p>";


helper.setText(htmlContent, true);
mailSender.send(message);
logger.info("Bill email sent to: {}", to);
}

    public void sendSupportQueryNotification(String name, String email, String subject, String message) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            helper.setTo("boomikamohan316@gmail.com");
            helper.setSubject("New Support Query: " + subject);
            helper.setText(
                "<h3>New Support Query</h3>" +
                "<p><strong>Name:</strong> " + name + "</p>" +
                "<p><strong>Email:</strong> " + email + "</p>" +
                "<p><strong>Subject:</strong> " + subject + "</p>" +
                "<p><strong>Message:</strong> " + message + "</p>",
                true
            );
            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send email notification: " + e.getMessage());
        }
    }
}
