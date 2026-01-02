package com.cinema.ticketbooking.domain.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ResCreateBookingDto {
    private Long userId;
    private String username;
    private Double price;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a", timezone = "GMT+7")
    private Instant createdAt;

    private Long paymentId;
    private String paymentUrl; // VNPay/Momo payment URL (null for CASH)
}
