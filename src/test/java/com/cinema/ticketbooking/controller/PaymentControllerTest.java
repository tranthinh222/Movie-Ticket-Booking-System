package com.cinema.ticketbooking.controller;

import com.cinema.ticketbooking.domain.Booking;
import com.cinema.ticketbooking.domain.Payment;
import com.cinema.ticketbooking.domain.request.ReqVNPayPaymentDto;
import com.cinema.ticketbooking.domain.response.ResPaymentDto;
import com.cinema.ticketbooking.domain.response.ResPaymentUrlDto;
import com.cinema.ticketbooking.service.PaymentService;
import com.cinema.ticketbooking.service.VNPayService;
import com.cinema.ticketbooking.util.constant.PaymentMethodEnum;
import com.cinema.ticketbooking.util.constant.PaymentStatusEnum;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {
    @Mock PaymentService paymentService;
    @Mock VNPayService vnPayService;
    @InjectMocks PaymentController controller;

    @Test
    void getPaymentsMapsDomainObjectsToResponseDtos() {
        Booking booking = new Booking();
        booking.setId(8L);
        Payment payment = payment(3L, booking);
        when(paymentService.getPaymentsByBookingId(8L)).thenReturn(List.of(payment));

        ResponseEntity<List<ResPaymentDto>> response = controller.getPaymentsByBooking(8L);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(8L, response.getBody().get(0).getBookingId());
        assertEquals(PaymentStatusEnum.PAID, response.getBody().get(0).getStatus());
    }

    @Test
    void createVNPayPaymentUsesFirstForwardedIp() throws Exception {
        ReqVNPayPaymentDto request = new ReqVNPayPaymentDto();
        request.setPaymentId(5L);
        request.setPrice(150000D);
        request.setOrderInfo("booking 5");
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        when(servletRequest.getHeader("X-Forwarded-For")).thenReturn("203.0.113.1, 10.0.0.1");
        when(vnPayService.createPaymentUrl(5L, 150000D, "booking 5", "203.0.113.1"))
                .thenReturn("https://pay.example/5");

        ResponseEntity<ResPaymentUrlDto> response = controller.createVNPayPayment(request, servletRequest);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("https://pay.example/5", response.getBody().getPaymentUrl());
    }

    @Test
    void callbackRedirectsToServiceResult() throws Exception {
        HttpServletResponse response = mock(HttpServletResponse.class);
        Map<String, String> params = Map.of("vnp_ResponseCode", "00");
        when(vnPayService.processCallback(params)).thenReturn("https://frontend/success");

        controller.vnpayCallback(params, response);

        verify(response).sendRedirect("https://frontend/success");
    }

    private Payment payment(Long id, Booking booking) {
        Payment payment = new Payment();
        payment.setId(id);
        payment.setBooking(booking);
        payment.setMethod(PaymentMethodEnum.VNPAY);
        payment.setStatus(PaymentStatusEnum.PAID);
        payment.setTransaction_time(Instant.now());
        return payment;
    }
}
