package com.cinema.ticketbooking.domain.request;

import com.cinema.ticketbooking.util.constant.GenderEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ReqUpdateUserDto {
    @NotNull(message = "user id is not null")
    private Long id;
    @NotBlank(message = "username is required")
    private String username;
    @Pattern(regexp = "^(0[0-9]{9,10})$", message = "Invalid phone number")
    private String phone;
    private GenderEnum gender;
    private String avatar;
}
