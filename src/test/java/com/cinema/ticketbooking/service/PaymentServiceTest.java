package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.Booking;
import com.cinema.ticketbooking.domain.Payment;
import com.cinema.ticketbooking.repository.PaymentRepository;
import com.cinema.ticketbooking.util.constant.PaymentMethodEnum;
import com.cinema.ticketbooking.util.constant.PaymentStatusEnum;
import com.cinema.ticketbooking.util.error.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {
    @Mock PaymentRepository repository;
    private PaymentService service;

    @BeforeEach
    void setUp() {
        service = new PaymentService(repository);
    }

    @Test
    void createPaymentInitializesAndPersistsAnUnpaidPayment() {
        Booking booking = new Booking();
        booking.setId(10L);
        when(repository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Instant before = Instant.now();
        Payment result = service.createPayment(booking, PaymentMethodEnum.CASH);

        assertSame(booking, result.getBooking());
        assertEquals(PaymentMethodEnum.CASH, result.getMethod());
        assertEquals(PaymentStatusEnum.UNPAID, result.getStatus());
        assertFalse(result.getTransaction_time().isBefore(before));
        verify(repository).save(result);
    }

    @Test
    void createPaymentRejectsNullBooking() {
        BadRequestException error = assertThrows(BadRequestException.class,
                () -> service.createPayment(null, PaymentMethodEnum.CASH));
        assertEquals("Booking cannot be null", error.getMessage());
        verifyNoInteractions(repository);
    }

    @Test
    void updatePaymentStatusUpdatesTimestampAndPersists() {
        Payment payment = new Payment();
        payment.setStatus(PaymentStatusEnum.UNPAID);
        when(repository.findById(4L)).thenReturn(Optional.of(payment));
        when(repository.save(payment)).thenReturn(payment);

        Payment result = service.updatePaymentStatus(4L, PaymentStatusEnum.PAID);

        assertEquals(PaymentStatusEnum.PAID, result.getStatus());
        assertNotNull(result.getTransaction_time());
        verify(repository).save(payment);
    }

    @Test
    void lookupMethodsReturnDataAndRejectMissingPayments() {
        Payment payment = new Payment();
        when(repository.findByBookingId(3L)).thenReturn(List.of(payment));
        when(repository.findById(2L)).thenReturn(Optional.empty());
        when(repository.findByTransactionRef("missing")).thenReturn(null);

        assertEquals(List.of(payment), service.getPaymentsByBookingId(3L));
        assertThrows(BadRequestException.class, () -> service.getPaymentById(2L));
        assertThrows(BadRequestException.class, () -> service.getPaymentByTransactionRef("missing"));
    }
}
