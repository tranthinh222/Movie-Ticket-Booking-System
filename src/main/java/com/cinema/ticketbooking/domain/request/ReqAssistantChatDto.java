package com.cinema.ticketbooking.domain.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReqAssistantChatDto {
    public enum Topic {
        MOVIES, MOVIE_DETAILS, SEATS
    }

    @NotBlank(message = "Vui lòng nhập tin nhắn.")
    @Size(max = 1000, message = "Tin nhắn không được quá 1000 ký tự.")
    private String message;
    @NotNull
    private Topic topic = Topic.MOVIES;
    @Positive
    private Long filmId;
    @Valid
    private ReqMovieRecommendationDto preferences = new ReqMovieRecommendationDto();
    @Valid
    private ReqSeatRecommendationDto seats;
}
