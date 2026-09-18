package com.cinema.ticketbooking.domain.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Data
public class ReqMovieRecommendationDto {
    @Size(max = 100, message = "Thể loại không được dài quá 100 ký tự.")
    private String genre;
    @Min(value = 1, message = "Thời lượng tối đa phải lớn hơn 0 phút.")
    @Max(value = 600, message = "Thời lượng tối đa không được vượt quá 600 phút.")
    private Integer maxDuration;
    private LocalDate date;
}
