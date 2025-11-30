package com.cinema.ticketbooking.domain.response;

import com.cinema.ticketbooking.util.constant.RoleEnum;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

import java.time.Instant;

@Data
public class ResUpdateUserDto {
    private String username;
    private String phone;
    private Instant updatedAt;
}
