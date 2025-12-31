package com.cinema.ticketbooking.domain.response;

import com.cinema.ticketbooking.util.constant.UserGenderEnum;
import lombok.Data;

import java.time.Instant;

@Data
public class ResUpdateUserDto {
    private String username;
    private String phone;
    private UserGenderEnum gender;
    private String avatar;
    private Instant updatedAt;
}
