package com.cinema.ticketbooking.domain.response;

import lombok.Data;

@Data
public class ResRegisterDto {
    private String accessToken;
    private String email;
    private String refreshToken;
}
