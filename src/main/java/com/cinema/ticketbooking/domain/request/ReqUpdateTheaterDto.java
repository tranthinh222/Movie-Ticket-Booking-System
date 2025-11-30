package com.cinema.ticketbooking.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ReqUpdateTheaterDto {
    @NotNull
    private Long id;
    @NotBlank(message = "Theater name is required")
    private String name;
    
}
