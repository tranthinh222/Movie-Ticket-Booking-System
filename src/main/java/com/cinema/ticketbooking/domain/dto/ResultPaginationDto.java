package com.cinema.ticketbooking.domain.dto;

import lombok.Data;

@Data
public class ResultPaginationDto {
    private Meta meta;
    private Object data;
}
