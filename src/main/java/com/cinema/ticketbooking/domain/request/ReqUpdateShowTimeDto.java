package com.cinema.ticketbooking.domain.request;

import com.cinema.ticketbooking.util.annotation.ValidShowTime;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@ValidShowTime
public class ReqUpdateShowTimeDto {
    @NotNull(message = "id must not be null")
    private Long id;
    @NotNull(message = "date is not null")
    private LocalDate date;
    @NotNull(message = "start time is not null")
    private LocalTime startTime;
    @NotNull(message = "end time is not null")
    private LocalTime endTime;
}
