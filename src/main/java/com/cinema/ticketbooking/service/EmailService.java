package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.Booking;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Base64;

@Service
public class EmailService {
    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@ticketbooking.com");
        message.setTo(toEmail);
        message.setSubject("Password Reset OTP");
        message.setText("Your OTP for password reset is: " + otp + "\n\nThis OTP will expire in 5 minutes.");

        mailSender.send(message);
    }

    public void sendBookingConfirmationWithQR(Booking booking) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("noreply@ticketbooking.com");
            helper.setTo(booking.getUser().getEmail());
            helper.setSubject("Booking Confirmation - Ticket #" + booking.getId());

            // Create HTML email content with embedded QR code
            String htmlContent = createBookingEmailHtml(booking);
            helper.setText(htmlContent, true);

            // Attach QR code as inline image
            if (booking.getQrCode() != null && !booking.getQrCode().isEmpty()) {
                byte[] qrCodeBytes = Base64.getDecoder().decode(booking.getQrCode());
                ByteArrayResource qrResource = new ByteArrayResource(qrCodeBytes);
                helper.addInline("qrcode", qrResource, "image/png");
            }

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send booking confirmation email: " + e.getMessage(), e);
        }
    }

    private String createBookingEmailHtml(Booking booking) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>");
        html.append("<html><head><style>");
        html.append("body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }");
        html.append(".container { max-width: 600px; margin: 0 auto; padding: 20px; }");
        html.append(".header { background-color: #4CAF50; color: white; padding: 20px; text-align: center; }");
        html.append(".content { padding: 20px; background-color: #f9f9f9; }");
        html.append(
                ".booking-info { background-color: white; padding: 15px; margin: 10px 0; border-left: 4px solid #4CAF50; }");
        html.append(".qr-section { text-align: center; margin: 20px 0; }");
        html.append(".qr-code { max-width: 300px; height: auto; }");
        html.append(".footer { text-align: center; margin-top: 20px; color: #777; font-size: 12px; }");
        html.append("</style></head><body>");

        html.append("<div class='container'>");
        html.append("<div class='header'>");
        html.append("<h1>Booking Confirmation</h1>");
        html.append("</div>");

        html.append("<div class='content'>");
        html.append("<p>Dear ").append(booking.getUser().getUsername()).append(",</p>");
        html.append("<p>Your booking has been confirmed successfully!</p>");

        html.append("<div class='booking-info'>");
        html.append("<h3>Booking Details</h3>");
        html.append("<p><strong>Booking ID:</strong> ").append(booking.getId()).append("</p>");
        html.append("<p><strong>Total Amount:</strong> ").append(String.format("%.0f VNĐ", booking.getTotal_price()))
                .append("</p>");
        html.append("<p><strong>Status:</strong> ").append(booking.getStatus()).append("</p>");
        html.append("</div>");

        if (booking.getQrCode() != null && !booking.getQrCode().isEmpty()) {
            html.append("<div class='qr-section'>");
            html.append("<h3>Your Ticket QR Code</h3>");
            html.append("<p>Please show this QR code at the cinema entrance:</p>");
            html.append("<img src='cid:qrcode' class='qr-code' alt='Booking QR Code'/>");
            html.append("</div>");
        }

        html.append("<p>Thank you for choosing our cinema!</p>");
        html.append("</div>");

        html.append("<div class='footer'>");
        html.append("<p>This is an automated email. Please do not reply.</p>");
        html.append("<p>&copy; 2026 Cinema Ticket Booking System</p>");
        html.append("</div>");

        html.append("</div>");
        html.append("</body></html>");

        return html.toString();
    }
}
