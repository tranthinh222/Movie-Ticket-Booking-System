package com.cinema.ticketbooking.domain.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class ResRegisterDto {
    private String accessToken;
    private String refreshToken;
    private ResUserJwtDto user;

}
