package com.cinema.ticketbooking.domain.request;

import com.cinema.ticketbooking.util.constant.PaymentMethodEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReqCreateBookingDto {
    @NotNull(message = "Payment method is required")
    private PaymentMethodEnum paymentMethod;
}
