package com.cinema.ticketbooking.domain.response;

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
}
