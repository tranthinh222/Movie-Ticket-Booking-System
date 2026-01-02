package com.cinema.ticketbooking.domain.response;

import com.cinema.ticketbooking.util.constant.GenderEnum;
import lombok.Data;

import java.time.Instant;

@Data
public class ResUpdateUserDto {
    private String username;
    private String phone;
    private GenderEnum gender;
    private String avatar;
    private Instant updatedAt;
}
