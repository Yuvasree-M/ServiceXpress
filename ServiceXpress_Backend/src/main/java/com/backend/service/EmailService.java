package com.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.backend.model.BookingRequest;

import jakarta.mail.MessagingException;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendBookingConfirmationEmail(String to, String customerName, BookingRequest booking) throws MessagingException {
        jakarta.mail.internet.MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject("Booking Confirmation - ServiceXpress");
        helper.setFrom("yuvasreemohan4sep2003@gmail.com"); // Must match spring.mail.username

        // Construct the HTML email body
        String htmlContent = "<html>" +
                "<body style='font-family: Poppins, sans-serif; color: #4B5563;'>" +
                "<div style='max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #E5E7EB; border-radius: 10px;'>" +
                "<h2 style='color: #F97316; text-align: center;'>Booking Confirmation</h2>" +
                "<p style='font-size: 16px;'>Dear <strong>" + customerName + "</strong>,</p>" +
                "<p style='font-size: 16px;'>Thank you for booking with ServiceXpress! Your service request has been successfully submitted. Below are the details of your booking:</p>" +
                "<table style='width: 100%; border-collapse: collapse; margin: 20px 0;'>" +
                "<tr><td style='padding: 8px; border: 1px solid #E5E7EB;'><strong>Booking ID:</strong></td><td style='padding: 8px; border: 1px solid #E5E7EB;'>" + booking.getId() + "</td></tr>" +
                "<tr><td style='padding: 8px; border: 1px solid #E5E7EB;'><strong>Name:</strong></td><td style='padding: 8px; border: 1px solid #E5E7EB;'>" + booking.getCustomerName() + "</td></tr>" +
                "<tr><td style='padding: 8px; border: 1px solid #E5E7EB;'><strong>Email:</strong></td><td style='padding: 8px; border: 1px solid #E5E7EB;'>" + booking.getCustomerEmail() + "</td></tr>" +
                "<tr><td style='padding: 8px; border: 1px solid #E5E7EB;'><strong>Mobile Number:</strong></td><td style='padding: 8px; border: 1px solid #E5E7EB;'>" + booking.getCustomerPhone() + "</td></tr>" +
                "<tr><td style='padding: 8px; border: 1px solid #E5E7EB;'><strong>Vehicle Registration Number:</strong></td><td style='padding: 8px; border: 1px solid #E5E7EB;'>" + booking.getVehicleRegistrationNumber() + "</td></tr>" +
                "<tr><td style='padding: 8px; border: 1px solid #E5E7EB;'><strong>Service Center ID:</strong></td><td style='padding: 8px; border: 1px solid #E5E7EB;'>" + booking.getServiceCenterId() + "</td></tr>" +
                "<tr><td style='padding: 8px; border: 1px solid #E5E7EB;'><strong>Vehicle Type ID:</strong></td><td style='padding: 8px; border: 1px solid #E5E7EB;'>" + booking.getVehicleTypeId() + "</td></tr>" +
                "<tr><td style='padding: 8px; border: 1px solid #E5E7EB;'><strong>Vehicle Model ID:</strong></td><td style='padding: 8px; border: 1px solid #E5E7EB;'>" + booking.getVehicleModelId() + "</td></tr>" +
                "<tr><td style='padding: 8px; border: 1px solid #E5E7EB;'><strong>Services:</strong></td><td style='padding: 8px; border: 1px solid #E5E7EB;'>" + booking.getServices() + "</td></tr>" +
                "<tr><td style='padding: 8px; border: 1px solid #E5E7EB;'><strong>Requested Date:</strong></td><td style='padding: 8px; border: 1px solid #E5E7EB;'>" + booking.getRequestedDate() + "</td></tr>" +
                "<tr><td style='padding: 8px; border: 1px solid #E5E7EB;'><strong>Address:</strong></td><td style='padding: 8px; border: 1px solid #E5E7EB;'>" + booking.getAddress() + "</td></tr>" +
                "<tr><td style='padding: 8px; border: 1px solid #E5E7EB;'><strong>Pick-up/Drop-off:</strong></td><td style='padding: 8px; border: 1px solid #E5E7EB;'>" + booking.getPickDropOption() + "</td></tr>" +
                (booking.getPickDropOption().equals("YES") ? (
                    "<tr><td style='padding: 8px; border: 1px solid #E5E7EB;'><strong>Pick-up/Drop-off Option:</strong></td><td style='padding: 8px; border: 1px solid #E5E7EB;'>" + booking.getPickupDropoffOption() + "</td></tr>" +
                    (booking.getPickupDropoffOption().equals("pickup") || booking.getPickupDropoffOption().equals("both") ? 
                        "<tr><td style='padding: 8px; border: 1px solid #E5E7EB;'><strong>Pick-up Address:</strong></td><td style='padding: 8px; border: 1px solid #E5E7EB;'>" + (booking.getPickupAddress() != null ? booking.getPickupAddress() : "N/A") + "</td></tr>" : "") +
                    (booking.getPickupDropoffOption().equals("dropoff") || booking.getPickupDropoffOption().equals("both") ? 
                        "<tr><td style='padding: 8px; border: 1px solid #E5E7EB;'><strong>Drop-off Address:</strong></td><td style='padding: 8px; border: 1px solid #E5E7EB;'>" + (booking.getDropAddress() != null ? booking.getDropAddress() : "N/A") + "</td></tr>" : "")
                ) : "") +
                "</table>" +
                "<p style='font-size: 16px; text-align: center;'>We will contact you soon to confirm your appointment. If you have any questions, feel free to reach out to us at <a href='mailto:info@servicexpress.com' style='color: #F97316;'>info@servicexpress.com</a>.</p>" +
                "<p style='font-size: 16px; text-align: center;'>Thank you for choosing ServiceXpress!</p>" +
                "<div style='text-align: center; margin-top: 20px;'>" +
                "<a href='http://localhost:8082/customer/bookings' style='background: #F97316; color: #FFFFFF; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>View Your Bookings</a>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";

        helper.setText(htmlContent, true); // true indicates HTML content
        mailSender.send(mimeMessage);
    }
}