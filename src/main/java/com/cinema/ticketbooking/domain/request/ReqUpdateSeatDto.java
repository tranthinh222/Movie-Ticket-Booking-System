package com.cinema.ticketbooking.domain.request;

import com.cinema.ticketbooking.util.constant.SeatStatusEnum;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReqUpdateSeatDto {
    @NotNull(message = "id is not null")
    private Long id;
    @NotNull (message = "seat row is not null")
    private String seatRow;
    @NotNull (message = "seat number is not null")
    @Min(value = 1, message = "seat number must be greater than 0")
    private int number;
    @NotNull (message = "seat type is not null")
    private SeatStatusEnum status;
}
