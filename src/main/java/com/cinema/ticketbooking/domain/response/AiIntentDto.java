package com.cinema.ticketbooking.domain.response;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.math.BigDecimal;

public record AiIntentDto(@NotNull Intent intent, @Size(max = 100) String genre,
        @Min(1) @Max(600) Integer maxDuration, LocalDate date, @Min(1) @Max(10) Integer people,
        @DecimalMin("0") BigDecimal budget, @Size(max = 40) String discountCode) {
    public AiIntentDto(Intent intent, String genre, Integer maxDuration, LocalDate date, Integer people,
            BigDecimal budget) {
        this(intent, genre, maxDuration, date, people, budget, null);
    }

    public enum Intent {
        MOVIES, MORE_MOVIES, MOVIE_DETAILS, SEATS, DISCOUNTS, GREETING, CLARIFY
    }
}
