package com.cinema.ticketbooking.domain.response;

import com.cinema.ticketbooking.util.constant.MethodEnum;
import com.cinema.ticketbooking.util.constant.PaymentStatusEnum;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ResPaymentDto {
    private Long id;
    private Long bookingId;
    private MethodEnum method;
    private PaymentStatusEnum status;
    private Instant transactionTime;
    private Instant createdAt;
}
