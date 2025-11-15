package com.cinema.ticketbooking.domain.dto;

import lombok.Data;

@Data
public class ResRegisterDto {
    private String accessToken;
    private String email;
    private String refreshToken;
}
