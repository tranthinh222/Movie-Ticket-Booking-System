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
    @Email(message = "Email không đúng định dạng.")
    @NotBlank(message = "Vui lòng nhập email.")
    private String email;

    @NotBlank(message = "Vui lòng nhập mật khẩu.")
    @jakarta.validation.constraints.Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự.")
    private String password;

    @NotBlank(message = "Vui lòng nhập tên người dùng.")
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
