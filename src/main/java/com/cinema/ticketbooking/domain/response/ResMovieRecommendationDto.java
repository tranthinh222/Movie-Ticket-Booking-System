package com.cinema.ticketbooking.domain.response;

public record ResMovieRecommendationDto(Long filmId, String name, String thumbnail,
                Long duration, String genre, String reason, java.math.BigDecimal minTicketPrice, String discountCode,
                java.math.BigDecimal discountAmount, java.math.BigDecimal finalTicketPrice) {
        public ResMovieRecommendationDto(Long filmId, String name, String thumbnail, Long duration, String genre,
                        String reason, java.math.BigDecimal price) {
                this(filmId, name, thumbnail, duration, genre, reason, price, null, java.math.BigDecimal.ZERO, price);
        }

        public ResMovieRecommendationDto(Long filmId, String name, String thumbnail, Long duration, String genre,
                        String reason) {
                this(filmId, name, thumbnail, duration, genre, reason, null);
        }
}
