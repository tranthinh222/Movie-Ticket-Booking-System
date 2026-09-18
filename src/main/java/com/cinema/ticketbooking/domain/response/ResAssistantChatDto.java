package com.cinema.ticketbooking.domain.response;
import java.util.List;
public record ResAssistantChatDto(String reply,List<ResMovieRecommendationDto> movies,
 List<ResSeatRecommendationDto> seatGroups,AiIntentDto memory,List<ResAssistantDiscountDto> discounts) {
 public ResAssistantChatDto(String reply,List<ResMovieRecommendationDto> movies,List<ResSeatRecommendationDto> seatGroups){this(reply,movies,seatGroups,null,List.of());}
 public ResAssistantChatDto(String reply,List<ResMovieRecommendationDto> movies,List<ResSeatRecommendationDto> seatGroups,AiIntentDto memory){this(reply,movies,seatGroups,memory,List.of());}
}
