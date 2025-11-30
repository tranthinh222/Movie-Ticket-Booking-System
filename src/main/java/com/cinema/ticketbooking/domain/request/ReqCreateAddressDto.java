package com.cinema.ticketbooking.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReqCreateAddressDto {
    @NotBlank(message = "City is required")
    private String city;
    @NotBlank(message = "Street number is required")
    private String street_number;
    @NotBlank(message = "Street name is required")
    private String street_name;
}
