package com.cinema.ticketbooking.domain.request;

import com.cinema.ticketbooking.domain.Address;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class ReqCreateTheaterDto {
    @NotNull
    @NotEmpty (message = "name must be not empty")
    private String name;
    @NotNull (message = "address id is required")
    private Long addressId;

}
