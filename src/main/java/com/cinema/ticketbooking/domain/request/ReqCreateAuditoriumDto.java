package com.cinema.ticketbooking.domain.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ReqCreateAuditoriumDto {
    @NotNull(message = "number is required")
    private Long number;
    @NotNull(message = "theater id must be not null")
    @Positive(message = "theaterId must be a positive number")
    private Long theaterId;
}
