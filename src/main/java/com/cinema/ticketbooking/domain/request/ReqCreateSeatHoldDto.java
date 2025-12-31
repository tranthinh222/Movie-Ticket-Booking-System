package com.cinema.ticketbooking.domain.request;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReqCreateSeatHoldDto {
    @NotNull(message = "showtimeId id is not null")
    private Long showtimeId;

    @NotNull(message = "seatId is not null")
    private List<Long> seatIds;
}
