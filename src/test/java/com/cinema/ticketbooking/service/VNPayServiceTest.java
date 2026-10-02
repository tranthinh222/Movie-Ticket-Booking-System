package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.Payment;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VNPayServiceTest {
    @Test
    void paymentDatesAlwaysUseVietnamTimezoneEvenWhenServerRunsInUtc() throws Exception {
        PaymentService payments = mock(PaymentService.class);
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setTransactionRef("txn-1");
        when(payments.getPaymentById(1L)).thenReturn(payment);

        VNPayService service = new VNPayService(payments, mock(BookingService.class),
                mock(EmailService.class), mock(QRCodeService.class));
        ReflectionTestUtils.setField(service, "vnpTmnCode", "test-code");
        ReflectionTestUtils.setField(service, "vnpHashSecret", "test-secret");
        ReflectionTestUtils.setField(service, "vnpUrl", "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html");
        ReflectionTestUtils.setField(service, "vnpReturnUrl", "https://example.com/callback");
        ReflectionTestUtils.setField(service, "vnpVersion", "2.1.0");
        ReflectionTestUtils.setField(service, "vnpCommand", "pay");

        TimeZone originalZone = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
            String url = service.createPaymentUrl(1L, 100_000D, "Test payment", "127.0.0.1");
            Map<String, String> query = Arrays.stream(new URI(url).getRawQuery().split("&"))
                    .map(value -> value.split("=", 2))
                    .collect(Collectors.toMap(parts -> parts[0],
                            parts -> URLDecoder.decode(parts[1], StandardCharsets.UTF_8)));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            LocalDateTime createdAt = LocalDateTime.parse(query.get("vnp_CreateDate"), formatter);
            LocalDateTime expiresAt = LocalDateTime.parse(query.get("vnp_ExpireDate"), formatter);
            LocalDateTime vietnamNow = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

            assertTrue(Math.abs(Duration.between(vietnamNow, createdAt).toSeconds()) < 5);
            assertEquals(15, Duration.between(createdAt, expiresAt).toMinutes());
        } finally {
            TimeZone.setDefault(originalZone);
        }
    }
}
