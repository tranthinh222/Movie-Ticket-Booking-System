package com.cinema.ticketbooking.domain.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReqLoginDto {
    @NotBlank(message = "email is not empty")
    @Email
    private String email;

    @NotBlank(message = "password is not empty")
    private String password;
}
