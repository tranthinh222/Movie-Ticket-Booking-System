package com.cinema.ticketbooking.service;
import com.cinema.ticketbooking.domain.Film;
import com.cinema.ticketbooking.domain.request.*;
import com.cinema.ticketbooking.domain.response.*;
import com.cinema.ticketbooking.repository.FilmRepository;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class AssistantChatServiceTest {
 MovieRecommendationService movies=mock(MovieRecommendationService.class);
 SeatRecommendationService seats=mock(SeatRecommendationService.class);
 FilmRepository films=mock(FilmRepository.class);
 AssistantChatService service=new AssistantChatService(movies,seats,films);
 @Test void usesRealRecommendationsAndHandlesEmptyResults(){var req=new ReqAssistantChatDto();req.setMessage("Phim");when(movies.recommend(any())).thenReturn(List.of(new ResMovieRecommendationDto(1L,"Phim",null,100L,"Hài","Có suất")));assertEquals(1,service.chat(req).movies().size());when(movies.recommend(any())).thenReturn(List.of());assertTrue(service.chat(req).reply().contains("Chưa có phim"));}
 @Test void asksForMissingContextWithoutGuessing(){var req=new ReqAssistantChatDto();req.setTopic(ReqAssistantChatDto.Topic.SEATS);assertTrue(service.chat(req).reply().contains("chọn một suất"));req.setTopic(ReqAssistantChatDto.Topic.MOVIE_DETAILS);assertTrue(service.chat(req).reply().contains("trang chi tiết"));verifyNoInteractions(movies,seats,films);}
 @Test void detailsUseDatabaseAndSeatRequestsDelegate(){var req=new ReqAssistantChatDto();req.setTopic(ReqAssistantChatDto.Topic.MOVIE_DETAILS);req.setFilmId(2L);var film=new Film();film.setId(2L);film.setName("Tên thật");film.setDescription("Nội dung có spoiler");when(films.findById(2L)).thenReturn(Optional.of(film));assertTrue(service.chat(req).reply().contains("Tên thật"));assertFalse(service.chat(req).reply().contains("spoiler"));req.setTopic(ReqAssistantChatDto.Topic.SEATS);var seatReq=new ReqSeatRecommendationDto();seatReq.setShowTimeId(3L);req.setSeats(seatReq);when(seats.recommend(seatReq)).thenReturn(List.of());assertTrue(service.chat(req).seatGroups().isEmpty());verify(seats).recommend(seatReq);}
 @Test void greetingsReturnOnlyWelcomeForEveryTopic(){
  for(var topic:ReqAssistantChatDto.Topic.values()){
   for(String text:List.of("hello","HI!"," Xin Chào!!! ","chao ban","hello bot 👋","xin   chào")){
    var req=new ReqAssistantChatDto();req.setTopic(topic);req.setMessage(text);
    var response=service.chat(req);assertTrue(response.reply().startsWith("Xin chào!"));
    assertTrue(response.movies().isEmpty());assertTrue(response.seatGroups().isEmpty());
   }
  }
  verifyNoInteractions(movies,seats,films);
 }
 @Test void greetingWithMovieRequestStillRunsRecommendation(){
  when(movies.recommend(any())).thenReturn(List.of());
  var req=new ReqAssistantChatDto();req.setMessage("Hello, gợi ý phim hài");
  service.chat(req);verify(movies).recommend(any());
 }
}
