package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.Booking;
import com.cinema.ticketbooking.domain.Payment;
import com.cinema.ticketbooking.repository.PaymentRepository;
import com.cinema.ticketbooking.util.constant.PaymentMethodEnum;
import com.cinema.ticketbooking.util.constant.PaymentStatusEnum;
import com.cinema.ticketbooking.util.error.BadRequestException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment createPayment(Booking booking, PaymentMethodEnum method) {
        if (booking == null) {
            throw new BadRequestException("Booking cannot be null");
        }

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setMethod(method);
        payment.setStatus(PaymentStatusEnum.UNPAID); // Default status for initial payment
        payment.setTransaction_time(Instant.now());

        return this.paymentRepository.save(payment);
    }

    public Payment updatePaymentStatus(Long paymentId, PaymentStatusEnum status) {
        Payment payment = this.paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BadRequestException("Payment not found with id: " + paymentId));

        payment.setStatus(status);
        payment.setTransaction_time(Instant.now());

        return this.paymentRepository.save(payment);
    }

    public List<Payment> getPaymentsByBookingId(Long bookingId) {
        return this.paymentRepository.findByBookingId(bookingId);
    }

    public Payment getPaymentById(Long paymentId) {
        return this.paymentRepository.findById(paymentId)
                .orElseThrow(() -> new BadRequestException("Payment not found with id: " + paymentId));
    }

    public Payment savePayment(Payment payment) {
        return this.paymentRepository.save(payment);
    }

    public Payment getPaymentByTransactionRef(String transactionRef) {
        Payment payment = this.paymentRepository.findByTransactionRef(transactionRef);
        if (payment == null) {
            throw new BadRequestException("Payment not found with transaction reference: " + transactionRef);
        }
        return payment;
    }
}
