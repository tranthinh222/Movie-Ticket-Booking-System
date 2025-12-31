package com.cinema.ticketbooking.domain.response;

import com.cinema.ticketbooking.util.constant.RoleEnum;
import com.cinema.ticketbooking.util.constant.UserGenderEnum;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResUserJwtDto {
    private Long id;
    private String username;
    private String email;
    private String phone;
    private UserGenderEnum gender;
    private String avatar;
    private RoleEnum role;
}
