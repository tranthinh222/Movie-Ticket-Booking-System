package com.cinema.ticketbooking.domain.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResRevenueDto {
    private Integer month;
    private Integer year;
    private Double totalRevenue;
    private Long totalBookings;
}
