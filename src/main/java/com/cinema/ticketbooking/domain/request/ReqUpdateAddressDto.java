package com.cinema.ticketbooking.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReqUpdateAddressDto {
    @NotNull
    private Long id;
    @NotBlank(message = "City is required")
    private String city;
    @NotBlank(message = "Street number is required")
    private String street_number;
    @NotBlank(message = "Street name is required")
    private String street_name;
}
