package com.cinema.ticketbooking.domain.response;

import lombok.Data;

@Data
public class ResSeatDto {
    private Long id;
    private String seatRow;
    private int number;
    private String status;
    private String seatVariantName;
}
