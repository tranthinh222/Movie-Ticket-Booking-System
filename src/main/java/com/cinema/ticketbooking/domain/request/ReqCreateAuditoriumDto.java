package com.cinema.ticketbooking.domain.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReqCreateAuditoriumDto {
    @NotNull(message = "name must be not null")
    private String name;
    @NotNull(message = "totalSeat is required")
    @Min(value = 1, message = "totalSeat must be greater than 0")
    private Long totalSeats;
    @NotNull(message = "theater id must be not null")
    private Long theaterId;
}
