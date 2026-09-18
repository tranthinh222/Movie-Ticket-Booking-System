package com.cinema.ticketbooking.domain.request;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
@Data
public class ReqSeatRecommendationDto {
 @NotNull(message="Vui lòng chọn suất chiếu.") @Positive private Long showTimeId;
 @Min(value=1,message="Số người phải từ 1 đến 10.") @Max(value=10,message="Số người phải từ 1 đến 10.") private int people=2;
 @DecimalMin(value="0",message="Ngân sách không được âm.") private BigDecimal budget;
}
