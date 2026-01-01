package com.cinema.ticketbooking.domain.request;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

import com.cinema.ticketbooking.util.constant.FilmStatusEnum;

@Data
public class ReqCreateFilmDto {

    @NotBlank(message = "Film name is required")
    private String name;
    private String director;
    private String actors;

    @NotNull(message = "Duration is required")
    @Positive(message = "Duration must be greater than 0")
    private Long duration;

    @NotNull(message = "Price is required")
    @PositiveOrZero(message = "Price must be >= 0")
    private Long price;

    @Size(max = 500, message = "Description too long")
    private String description;

    @NotBlank(message = "Genre is required")
    private String genre;

    @NotBlank(message = "Language is required")
    private String language;

    @NotNull(message = "status is required")
    private FilmStatusEnum status;

    private LocalDate release_date;

    private String thumbnail;
}
