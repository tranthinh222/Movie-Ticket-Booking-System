package com.cinema.ticketbooking.domain.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResPaymentUrlDto {
    private String paymentUrl;
    private Long paymentId;
    private String message;
}
