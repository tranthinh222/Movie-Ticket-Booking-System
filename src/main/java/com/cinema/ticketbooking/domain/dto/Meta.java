package com.cinema.ticketbooking.domain.dto;

import lombok.Data;

@Data
public class Meta {
    private long currentPage;
    private long pageSize;
    private long totalPages;
    private long totalItems;
}
