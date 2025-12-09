package com.cinema.ticketbooking.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReqCreateTheaterDto {
    @NotBlank(message = "theater name is required")
    private String name;
    @NotNull(message = "address id is required")
    private Long addressId;

}
