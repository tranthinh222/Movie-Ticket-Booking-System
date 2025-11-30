package com.cinema.ticketbooking.domain.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
public class ReqUpdateFilmDto {
    @NotNull
    private Long id;
    @NotBlank(message = "Film name is required")
    private String name;
    private Long duration;
    private Long price;

    private String description;
    private String genre;
    private String language;
    private Instant release_date;
    private Long rating;
}
