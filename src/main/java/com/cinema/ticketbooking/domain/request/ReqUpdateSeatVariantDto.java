package com.cinema.ticketbooking.domain.request;

import com.cinema.ticketbooking.util.constant.SeatTypeEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReqUpdateSeatVariantDto {
    @NotNull(message = "id is not null")
    private Long id;
    @NotNull(message = "seat type is not null")
    private SeatTypeEnum seatType;
    @NotNull(message = "bonus is not null")
    private double bonus;
}
