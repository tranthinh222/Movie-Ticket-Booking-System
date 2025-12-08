package com.cinema.ticketbooking.domain.request;

import com.cinema.ticketbooking.util.constant.SeatTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReqCreateSeatVariantDto {
    @NotNull(message = "seat id is not null")
    private Long seatId;
    @NotNull(message = "seat type is not null")
    private SeatTypeEnum seatType;
    @NotNull(message = "bonus is not null")
    private double bonus;
}
