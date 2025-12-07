package com.cinema.ticketbooking.domain.response;

import lombok.Data;

import java.time.Instant;

@Data
public class ResUpdateUserDto {
    private String username;
    private String phone;
    private Instant updatedAt;
}
