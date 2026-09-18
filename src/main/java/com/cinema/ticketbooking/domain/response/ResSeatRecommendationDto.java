package com.cinema.ticketbooking.domain.response;
import java.util.List;
import java.math.BigDecimal;
public record ResSeatRecommendationDto(Long showTimeId,List<Long> seatIds,List<String> labels,BigDecimal totalPrice,String reason) {}
