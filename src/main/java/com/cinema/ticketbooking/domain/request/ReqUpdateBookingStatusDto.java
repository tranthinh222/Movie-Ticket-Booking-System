package com.cinema.ticketbooking.domain.request;

import com.cinema.ticketbooking.util.constant.BookingStatusEnum;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReqUpdateBookingStatusDto {
    @NotNull(message = "Status is required")
    private BookingStatusEnum status;
}
