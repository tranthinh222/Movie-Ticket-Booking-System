package com.cinema.ticketbooking.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginDto {
    @NotBlank(message = "email is not empty")
    private String email;

    @NotBlank(message = "password is not empty")
    private String password;
}
