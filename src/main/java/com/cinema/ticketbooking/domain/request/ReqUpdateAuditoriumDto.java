package com.cinema.ticketbooking.domain.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ReqUpdateAuditoriumDto {
    @NotNull(message = "id must be not null")
    private Long id;
    @NotBlank(message = "name is required")
    private String name;
    @NotNull(message = "totalSeat is required")
    @Positive(message = "totalSeats must be > 0")
    private Long totalSeat;
}
