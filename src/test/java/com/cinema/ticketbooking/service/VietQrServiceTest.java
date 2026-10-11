package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.util.error.BadRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class VietQrServiceTest {
    @Test
    void generateQrUrlEncodesQueryValuesAndSanitizesPathValues() {
        VietQrService service = configured("9704 15", "123.456", "NGUYỄN VĂN A", "compact-2!");

        String result = service.generateQrUrl(125000, "Thanh toán vé #42");

        assertEquals("https://img.vietqr.io/image/970415-123456-compact-2.png?amount=125000"
                + "&addInfo=Thanh+to%C3%A1n+v%C3%A9+%2342"
                + "&accountName=NGUY%E1%BB%84N+V%C4%82N+A", result);
    }

    @Test
    void generateQrUrlRejectsIncompleteConfiguration() {
        VietQrService service = configured("", "123", "Owner", "compact2");
        BadRequestException error = assertThrows(BadRequestException.class,
                () -> service.generateQrUrl(1, "payment"));
        assertEquals("Thông tin tài khoản VietQR chưa được cấu hình", error.getMessage());
    }

    private VietQrService configured(String bankId, String accountNo, String accountName, String template) {
        VietQrService service = new VietQrService();
        ReflectionTestUtils.setField(service, "bankId", bankId);
        ReflectionTestUtils.setField(service, "accountNo", accountNo);
        ReflectionTestUtils.setField(service, "accountName", accountName);
        ReflectionTestUtils.setField(service, "template", template);
        return service;
    }
}
