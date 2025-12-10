package com.cinema.ticketbooking.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReqUpdateTheaterDto {
    @NotNull (message = "id must not be null")
    private Long id;
    private String name;
}
