package com.cinema.ticketbooking.domain.request;

import com.cinema.ticketbooking.util.constant.MethodEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqCreatePaymentDto {
    @NotNull(message = "Payment method is required")
    private MethodEnum method;
}
