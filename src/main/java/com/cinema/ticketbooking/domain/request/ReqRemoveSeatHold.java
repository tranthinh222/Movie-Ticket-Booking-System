package com.cinema.ticketbooking.domain.request;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

public class ReqRemoveSeatHold {
    @Data
    public class ReqCreateSeatHoldDto {
        @NotNull(message = "seatId is not null")
        private List<Long> seatIds;
    }
}
