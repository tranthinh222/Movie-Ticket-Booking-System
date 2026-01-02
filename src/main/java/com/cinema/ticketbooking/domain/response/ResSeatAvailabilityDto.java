package com.cinema.ticketbooking.domain.response;

import com.cinema.ticketbooking.util.constant.SeatStatusEnum;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResSeatAvailabilityDto {
    private Long seatId;
    private String seatRow;
    private int number;
    private SeatStatusEnum status; // AVAILABLE, HOLD, BOOKED
    private Long seatVariantId;
    private String seatVariantName;
    private Double basePrice;
    private Double bonus;
    private Double totalPrice;
}
