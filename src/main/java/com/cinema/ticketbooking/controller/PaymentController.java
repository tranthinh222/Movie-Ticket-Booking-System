package com.cinema.ticketbooking.controller;

import com.cinema.ticketbooking.domain.Payment;
import com.cinema.ticketbooking.domain.request.ReqCreatePaymentDto;
import com.cinema.ticketbooking.domain.request.ReqVNPayPaymentDto;
import com.cinema.ticketbooking.domain.response.ResPaymentDto;
import com.cinema.ticketbooking.domain.response.ResPaymentUrlDto;
import com.cinema.ticketbooking.service.BookingService;
import com.cinema.ticketbooking.service.PaymentService;
import com.cinema.ticketbooking.service.VNPayService;
import com.cinema.ticketbooking.util.annotation.ApiMessage;
import com.cinema.ticketbooking.util.error.BadRequestException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class PaymentController {
    private final PaymentService paymentService;
    private final VNPayService vnPayService;

    public PaymentController(PaymentService paymentService, VNPayService vnPayService) {
        this.paymentService = paymentService;
        this.vnPayService = vnPayService;
    }

    @GetMapping("/bookings/{bookingId}/payments")
    @ApiMessage("Get all payments for booking")
    public ResponseEntity<List<ResPaymentDto>> getPaymentsByBooking(@PathVariable("bookingId") Long bookingId) {
        List<Payment> payments = this.paymentService.getPaymentsByBookingId(bookingId);
        List<ResPaymentDto> response = payments.stream()
                .map(this::convertToResPaymentDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/payments/{id}")
    @ApiMessage("Get payment by id")
    public ResponseEntity<ResPaymentDto> getPaymentById(@PathVariable("id") Long id) {
        Payment payment = this.paymentService.getPaymentById(id);
        return ResponseEntity.ok(convertToResPaymentDto(payment));
    }

    // @PutMapping("/payments/{id}/status")
    // @ApiMessage("Update payment status")
    // public ResponseEntity<ResPaymentDto> updatePaymentStatus(
    // @PathVariable("id") Long id,
    // @RequestParam PaymentStatusEnum status) {
    // Payment payment = this.paymentService.updatePaymentStatus(id, status);
    // return ResponseEntity.ok(convertToResPaymentDto(payment));
    // }

    private ResPaymentDto convertToResPaymentDto(Payment payment) {
        ResPaymentDto dto = new ResPaymentDto();
        dto.setId(payment.getId());
        dto.setBookingId(payment.getBooking().getId());
        dto.setMethod(payment.getMethod());
        dto.setStatus(payment.getStatus());
        dto.setTransactionTime(payment.getTransaction_time());
        dto.setCreatedAt(payment.getCreatedAt());
        return dto;
    }

    @PostMapping("/payments/vnpay/create")
    @ApiMessage("Create VNPay payment URL")
    public ResponseEntity<ResPaymentUrlDto> createVNPayPayment(
            @Valid @RequestBody ReqVNPayPaymentDto request,
            HttpServletRequest httpRequest) {
        try {
            String ipAddress = getClientIp(httpRequest);
            String paymentUrl = vnPayService.createPaymentUrl(
                    request.getPaymentId(),
                    request.getPrice(),
                    request.getOrderInfo(),
                    ipAddress);

            ResPaymentUrlDto response = new ResPaymentUrlDto(
                    paymentUrl,
                    request.getPaymentId(),
                    "Payment URL created successfully");

            return ResponseEntity.ok(response);
        } catch (UnsupportedEncodingException e) {
            throw new BadRequestException("Error creating payment URL: " + e.getMessage());
        }
    }

    @GetMapping("/payments/vnpay/callback")
    public void vnpayCallback(@RequestParam Map<String, String> params, HttpServletResponse response) throws Exception {
        String redirectUrl = vnPayService.processCallback(params);
        response.sendRedirect(redirectUrl);
    }

    private String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("X-Real-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        // If there are multiple IPs, get the first one
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        return ipAddress;
    }
}
