package com.cinema.ticketbooking.domain.response;

import java.util.List;

public record ResAssistantChatDto(String reply, List<ResMovieRecommendationDto> movies,
        List<ResSeatRecommendationDto> seatGroups) {
}
