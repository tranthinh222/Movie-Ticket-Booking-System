package com.cinema.ticketbooking.domain.response;

import lombok.Data;

@Data
public class ResultPaginationDto {
    private Meta meta;
    private Object data;

    @Data
    public static class Meta{
        private long currentPage;
        private long pageSize;
        private long totalPages;
        private long totalItems;
    }
}
