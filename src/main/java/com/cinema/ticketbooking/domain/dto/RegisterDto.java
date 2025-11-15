package com.cinema.ticketbooking.domain.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegisterDto {
    @Email
    @NotBlank(message = "email is not empty")
    private String email;

    @NotBlank(message = "password is not empty")
    private String password;

    @NotBlank(message = "username is not empty")
    private String username;

    private String phone;

    private String role = "USER";

}
