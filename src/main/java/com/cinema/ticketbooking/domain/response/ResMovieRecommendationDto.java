package com.cinema.ticketbooking.domain.response;

public record ResMovieRecommendationDto(Long filmId, String name, String thumbnail,
        Long duration, String genre, String reason, java.math.BigDecimal minTicketPrice) {
 public ResMovieRecommendationDto(Long filmId,String name,String thumbnail,Long duration,String genre,String reason){this(filmId,name,thumbnail,duration,genre,reason,null);}
}
