package com.cinema.ticketbooking.domain.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReqVNPayPaymentDto {
    @NotNull(message = "Payment ID is required")
    private Long paymentId;

    @NotNull(message = "Price is required")
    private Double price;

    private String orderInfo;
    private String locale; // Default: vn, en for English
}
