package com.cinema.ticketbooking.service;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class QRCodeServiceTest {
    private final QRCodeService service = new QRCodeService();

    @Test
    void generateQRCodeBase64ProducesReadablePngAtRequestedSize() throws Exception {
        byte[] bytes = Base64.getDecoder().decode(service.generateQRCodeBase64("BOOKING-42", 180, 120));
        var image = ImageIO.read(new ByteArrayInputStream(bytes));

        assertNotNull(image);
        assertEquals(180, image.getWidth());
        assertEquals(120, image.getHeight());
    }

    @Test
    void bookingHelpersGenerateNonEmptyQrCodes() {
        assertFalse(service.generateBookingQRCode(7L).isBlank());
        assertFalse(service.generateBookingQRCodeWithDetails(7L, "user@example.com", 125000D).isBlank());
    }

}
