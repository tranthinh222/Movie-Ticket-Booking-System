package com.cinema.ticketbooking.domain.response;

import lombok.Data;

@Data
public class ResLoginDto {
    private String accessToken;
    private ResUserJwtDto user;

}
