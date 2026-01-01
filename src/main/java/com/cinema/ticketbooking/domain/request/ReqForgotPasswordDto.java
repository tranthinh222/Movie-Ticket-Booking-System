package com.cinema.ticketbooking.domain.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqForgotPasswordDto {
    @NotBlank(message = "Email is required")
    @Email(message = "Email is invalid")
    private String email;
}
