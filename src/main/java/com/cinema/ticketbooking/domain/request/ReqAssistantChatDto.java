package com.cinema.ticketbooking.domain.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;
import com.cinema.ticketbooking.domain.response.AiIntentDto;

@Data
public class ReqAssistantChatDto {
    public enum Topic {
        MOVIES, MOVIE_DETAILS, SEATS, DISCOUNTS
    }

    public record Turn(@NotNull Role role, @NotBlank @Size(max=1000) String text) {
        public enum Role { user, assistant }
    }
    @NotNull @Size(max=10) private List<@Valid @NotNull Turn> history=List.of();
    @Valid private AiIntentDto memory;
    @Positive private Long memoryFilmId;
    @Positive private Long memoryShowTimeId;
    @NotNull @Size(max=50) private List<@Positive @NotNull Long> seenFilmIds=List.of();
    private boolean useAi;

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
