package com.cinema.ticketbooking.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Service
public class QRCodeService {

    /**
     * Generate QR code as Base64 string
     * 
     * @param data   Data to encode in QR code (e.g., booking ID, booking info JSON)
     * @param width  QR code width in pixels
     * @param height QR code height in pixels
     * @return Base64 encoded QR code image (PNG format)
     */
    public String generateQRCodeBase64(String data, int width, int height) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();

            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hints.put(EncodeHintType.MARGIN, 1);

            BitMatrix bitMatrix = qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, width, height, hints);
            BufferedImage qrImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            // Convert BufferedImage to Base64
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "PNG", baos);
            byte[] imageBytes = baos.toByteArray();

            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (WriterException | IOException e) {
            throw new RuntimeException("Failed to generate QR code: " + e.getMessage(), e);
        }
    }

    /**
     * Generate QR code for booking
     * 
     * @param bookingId Booking ID
     * @return Base64 encoded QR code
     */
    public String generateBookingQRCode(Long bookingId) {
        // Create QR code data - can be customized based on requirements
        String qrData = "BOOKING-" + bookingId;
        return generateQRCodeBase64(qrData, 300, 300);
    }

    /**
     * Generate QR code with booking details in JSON format
     * 
     * @param bookingId  Booking ID
     * @param userEmail  User email
     * @param totalPrice Total price
     * @return Base64 encoded QR code
     */
    public String generateBookingQRCodeWithDetails(Long bookingId, String userEmail, Double totalPrice) {
        // Create JSON-like string for QR code
        String qrData = String.format("{\"bookingId\":%d,\"email\":\"%s\",\"total\":%.2f}",
                bookingId, userEmail, totalPrice);
        return generateQRCodeBase64(qrData, 300, 300);
    }
}
