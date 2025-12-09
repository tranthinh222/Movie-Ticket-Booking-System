package com.cinema.ticketbooking.domain.request;

import com.cinema.ticketbooking.util.constant.SeatTypeEnum;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReqCreateSeatVariantDto {
    @NotNull(message = "seat type is not null")
    private SeatTypeEnum seatType;

    @NotNull(message = "base price is not null")
    @Min(value = 0, message = "base price must be >= 0")
    private double basePrice;

    @NotNull(message = "bonus is not null")
    @Min(value = 0, message = "bonus price must be >= 0")
    private double bonus;
}
