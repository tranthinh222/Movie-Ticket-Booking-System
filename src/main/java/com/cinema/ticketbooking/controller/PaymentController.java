package com.cinema.ticketbooking.controller;

import com.cinema.ticketbooking.domain.Payment;
import com.cinema.ticketbooking.domain.request.ReqCreatePaymentDto;
import com.cinema.ticketbooking.domain.response.ResPaymentDto;
import com.cinema.ticketbooking.service.BookingService;
import com.cinema.ticketbooking.service.PaymentService;
import com.cinema.ticketbooking.util.annotation.ApiMessage;
import com.cinema.ticketbooking.util.error.BadRequestException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
public class PaymentController {
    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
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
}
