package com.cinema.ticketbooking.domain.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class ReqUpdateAddressDto {
    @NotNull(message = "id must not be null")
    @Positive(message = "id must be greater than 0")
    private Long id;
    private String city;
    private String street_number;
    private String street_name;
}
