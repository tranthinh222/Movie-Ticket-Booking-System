package com.cinema.ticketbooking.domain.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReqCreateBookingDto {
    @NotNull(message = "number is required")
    private Long userId;
}
