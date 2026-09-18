package com.cinema.ticketbooking.domain.response;
import com.cinema.ticketbooking.domain.Discount;
import java.math.BigDecimal;
import java.time.LocalDate;
public record ResAssistantDiscountDto(String code,String title,String description,Discount.Type type,
 BigDecimal value,BigDecimal minOrder,BigDecimal maxDiscount,LocalDate startsOn,LocalDate endsOn) {
 public static ResAssistantDiscountDto from(Discount d){return new ResAssistantDiscountDto(d.getCode(),d.getTitle(),d.getDescription(),d.getType(),d.getValue(),d.getMinOrder(),d.getMaxDiscount(),d.getStartsOn(),d.getEndsOn());}
}
