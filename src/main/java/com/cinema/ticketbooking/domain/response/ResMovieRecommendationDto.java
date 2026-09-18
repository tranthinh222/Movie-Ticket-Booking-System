package com.cinema.ticketbooking.domain.response;

public record ResMovieRecommendationDto(Long filmId, String name, String thumbnail,
        Long duration, String genre, String reason) {}
