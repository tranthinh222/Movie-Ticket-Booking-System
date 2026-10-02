package com.cinema.ticketbooking.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cinema.ticketbooking.util.error.BadRequestException;

@Service
public class VietQrService {
    @Value("${vietqr.bank-id:}")
    private String bankId;

    @Value("${vietqr.account-no:}")
    private String accountNo;

    @Value("${vietqr.account-name:}")
    private String accountName;

    @Value("${vietqr.template:compact2}")
    private String template;

    public String generateQrUrl(long amount, String paymentContent) {
        if (bankId.isBlank() || accountNo.isBlank() || accountName.isBlank()) {
            throw new BadRequestException("Thông tin tài khoản VietQR chưa được cấu hình");
        }
        return "https://img.vietqr.io/image/" + encodePath(bankId) + "-" + encodePath(accountNo) + "-"
                + encodePath(template) + ".png?amount=" + amount
                + "&addInfo=" + encode(paymentContent)
                + "&accountName=" + encode(accountName);
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String encodePath(String value) {
        return value.replaceAll("[^A-Za-z0-9_-]", "");
    }
}
