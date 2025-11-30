package com.cinema.ticketbooking.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.Instant;
import java.time.LocalDate;

@Data
public class ReqCreateFilmDto {
    @NotBlank(message = "Film name is required")
    private String name;
    private Long duration;
    private Long price;

    private String description;
    private String genre;
    private String language;
    private LocalDate release_date;
    private Long rating;

}
