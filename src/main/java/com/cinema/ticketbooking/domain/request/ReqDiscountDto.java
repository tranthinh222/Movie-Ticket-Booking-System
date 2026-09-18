package com.cinema.ticketbooking.domain.request;
import com.cinema.ticketbooking.domain.Discount;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
@Data
public class ReqDiscountDto {
 @NotBlank @Pattern(regexp="[A-Za-z0-9_-]{3,40}",message="Mã ưu đãi cần 3–40 ký tự chữ, số, gạch ngang hoặc gạch dưới.") private String code;
 @NotBlank @Size(max=255) private String title;
 @Size(max=4000) private String description;
 @Size(max=2000) @Pattern(regexp="^(https?://[^\\s]+)?$") private String image;
 @NotNull private Discount.Type type;
 @NotNull @DecimalMin("0.01") @DecimalMax("9999999999999") private BigDecimal value;
 @NotNull @DecimalMin("0") @DecimalMax("9999999999999") private BigDecimal minOrder;
 @DecimalMin("0.01") @DecimalMax("9999999999999") private BigDecimal maxDiscount;
 @NotNull private LocalDate startsOn;
 @NotNull private LocalDate endsOn;
 private boolean active;
}
