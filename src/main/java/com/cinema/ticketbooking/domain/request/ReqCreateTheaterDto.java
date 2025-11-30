package com.cinema.ticketbooking.domain.request;

import com.cinema.ticketbooking.domain.Address;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class ReqCreateTheaterDto {
    @NotNull
    private String name;
    private Long addressId;

}
