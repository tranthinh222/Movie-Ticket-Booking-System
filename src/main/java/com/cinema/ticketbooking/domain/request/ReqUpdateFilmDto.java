package com.cinema.ticketbooking.domain.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;
import com.cinema.ticketbooking.util.constant.FilmStatusEnum;
@Data
public class ReqUpdateFilmDto {
    @NotNull(message = "id must not be null")
    @Positive(message = "id must be greater than 0")
    private Long id;
    private String name;
    private String director;
    private String actors;
    @Positive(message = "duration must be greater than 0")
    private Long duration;
    @PositiveOrZero(message = "price must be greater than or equal to 0")
    private Long price;

    private FilmStatusEnum status;

    private String description;
    private String genre;
    private String language;
    @PastOrPresent(message = "release date cannot be in the future")
    private LocalDate release_date;
    @Min(value = 1, message = "rating must be at least 1")
    @Max(value = 10, message = "rating cannot be greater than 10")
    private Long rating;

    private String thumbnail;
}
