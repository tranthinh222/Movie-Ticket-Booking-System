package com.cinema.ticketbooking.domain.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReqLoginDto {
    @NotBlank(message = "Vui lòng nhập email.")
    @Email(message = "Email không đúng định dạng.")
    private String email;

    @NotBlank(message = "Vui lòng nhập mật khẩu.")
    private String password;
}
