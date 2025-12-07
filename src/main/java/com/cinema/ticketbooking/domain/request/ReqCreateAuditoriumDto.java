package com.cinema.ticketbooking.domain.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ReqCreateAuditoriumDto {
    @NotBlank(message = "name is required")
    private String name;
    @NotNull(message = "totalSeat must be not null")
    @Positive(message = "totalSeats must be > 0")
    private Long totalSeats;
    @NotNull(message = "theater id must be not null")
    @Positive(message = "theaterId must be a positive number")
    private Long theaterId;
}
