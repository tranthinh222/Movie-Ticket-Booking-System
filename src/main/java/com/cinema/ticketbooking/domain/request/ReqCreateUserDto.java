package com.cinema.ticketbooking.domain.request;

import com.cinema.ticketbooking.util.constant.RoleEnum;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ReqCreateUserDto {
    @NotBlank (message = "username is required")
    private String username;
    @Email
    @NotBlank (message = "email is required")
    private String email;
    @NotBlank (message = "password is required")
    private String password;
    @Pattern(
            regexp = "^(0[0-9]{9,10})$",
            message = "Invalid phone number"
    )
    private String phone;
    private RoleEnum role;
}
