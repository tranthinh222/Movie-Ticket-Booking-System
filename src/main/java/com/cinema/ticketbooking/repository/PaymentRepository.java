package com.cinema.ticketbooking.repository;

import com.cinema.ticketbooking.domain.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    List<Payment> findByBookingId(Long bookingId);

    Payment findByTransactionRef(String transactionRef);
}
