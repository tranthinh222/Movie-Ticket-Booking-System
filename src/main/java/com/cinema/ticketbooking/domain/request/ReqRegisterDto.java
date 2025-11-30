package com.cinema.ticketbooking.domain.request;

import com.cinema.ticketbooking.util.constant.RoleEnum;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReqRegisterDto {
    @Email
    @NotBlank(message = "email is not empty")
    private String email;

    @NotBlank(message = "password is not empty")
    private String password;

    @NotBlank(message = "username is not empty")
    private String username;

    private String phone;

    @Enumerated(EnumType.STRING)
    private RoleEnum role = RoleEnum.CUSTOMER;

}
