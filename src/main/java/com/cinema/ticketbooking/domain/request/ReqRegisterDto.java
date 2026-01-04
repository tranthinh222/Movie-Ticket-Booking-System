package com.cinema.ticketbooking.domain.request;

import com.cinema.ticketbooking.util.constant.RoleEnum;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrePersist;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

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


    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a", timezone = "GMT+7")
    private Instant createdAt;

    @PrePersist
    public void handleBeforeCreated() {
        this.createdAt = Instant.now();
    }

}
