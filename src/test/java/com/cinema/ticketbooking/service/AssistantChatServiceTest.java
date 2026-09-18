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
 GeminiIntentService gemini=mock(GeminiIntentService.class);
 DiscountService discounts=mock(DiscountService.class);
 AssistantChatService service=new AssistantChatService(movies,seats,films,gemini,discounts);
 @Test void discountsReadCurrentDatabaseAndFilterRequestedCode(){
  var offer=new com.cinema.ticketbooking.domain.Discount();offer.setCode("CINE10");offer.setTitle("Giảm vé");
  when(discounts.list(false)).thenReturn(List.of(offer));
  var req=new ReqAssistantChatDto();req.setUseAi(true);req.setMessage("Điều kiện CINE10");
  when(gemini.interpret(req)).thenReturn(new AiIntentDto(AiIntentDto.Intent.DISCOUNTS,null,null,null,null,null,"cine10"));
  var response=service.chat(req);assertEquals("CINE10",response.discounts().get(0).code());assertTrue(response.movies().isEmpty());verifyNoInteractions(movies,seats,films);
  when(gemini.interpret(req)).thenReturn(new AiIntentDto(AiIntentDto.Intent.DISCOUNTS,null,null,null,null,null,"OLD"));
  assertTrue(service.chat(req).discounts().isEmpty());assertTrue(service.chat(req).reply().contains("không còn hiệu lực"));
 }
 @Test void emptyDiscountsDoNotFallBackToMovies(){
  when(discounts.list(false)).thenReturn(List.of());var req=new ReqAssistantChatDto();req.setTopic(ReqAssistantChatDto.Topic.DISCOUNTS);
  assertTrue(service.chat(req).reply().contains("chưa có ưu đãi"));verifyNoInteractions(movies,seats,films,gemini);
 }
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
 @Test void aiRoutesParsedFiltersAndDoesNotGuessUnknownIntent(){
  var req=new ReqAssistantChatDto();req.setUseAi(true);req.setMessage("Phim hài dưới hai tiếng");
  when(gemini.interpret(req)).thenReturn(new AiIntentDto(AiIntentDto.Intent.MOVIES,"Hài",120,null,null,null));when(movies.recommend(any())).thenReturn(List.of());
  service.chat(req);verify(movies).recommend(argThat(p->"Hài".equals(p.getGenre())&&p.getMaxDuration()==120));
  clearInvocations(movies);when(gemini.interpret(req)).thenReturn(new AiIntentDto(AiIntentDto.Intent.CLARIFY,null,null,null,null,null));service.chat(req);verifyNoInteractions(movies);
 }
 @Test void retainsFullAiSnapshotAndAllowsRemovingCondition(){
  when(movies.recommend(any())).thenReturn(List.of());var req=new ReqAssistantChatDto();req.setUseAi(true);req.setMessage("Dưới hai tiếng");
  req.setMemory(new AiIntentDto(AiIntentDto.Intent.MOVIES,"Hài",null,null,null,null));
  when(gemini.interpret(req)).thenReturn(new AiIntentDto(AiIntentDto.Intent.MOVIES,"Hài",120,null,null,null));
  var response=service.chat(req);assertEquals("Hài",response.memory().genre());assertEquals(120,response.memory().maxDuration());
  req.setMessage("Bỏ giới hạn thời lượng");when(gemini.interpret(req)).thenReturn(new AiIntentDto(AiIntentDto.Intent.MOVIES,"Hài",null,null,null,null));assertNull(service.chat(req).memory().maxDuration());
 }
 @Test void anotherMovieUsesSeenIdsAndGreetingKeepsMemory(){
  var req=new ReqAssistantChatDto();req.setUseAi(true);req.setMessage("Phim khác");req.setSeenFilmIds(List.of(14L));
  when(gemini.interpret(req)).thenReturn(new AiIntentDto(AiIntentDto.Intent.MORE_MOVIES,"Lịch Sử",300,null,null,null));when(movies.recommend(any(),anySet())).thenReturn(List.of());
  service.chat(req);verify(movies).recommend(any(),eq(Set.of(14L)));
  req.setMessage("Hello");var before=req.getMemory();assertEquals(before,service.chat(req).memory());
 }
 @Test void changedShowClearsOldSeatMemoryBeforeCallingAi(){
  var req=new ReqAssistantChatDto();req.setUseAi(true);req.setMessage("4 người");req.setMemory(new AiIntentDto(AiIntentDto.Intent.SEATS,null,null,null,2,null));req.setMemoryShowTimeId(1L);
  var selected=new ReqSeatRecommendationDto();selected.setShowTimeId(2L);req.setSeats(selected);req.setHistory(List.of(new ReqAssistantChatDto.Turn(ReqAssistantChatDto.Turn.Role.user,"Suất cũ")));
  when(gemini.interpret(req)).thenAnswer(call->{assertNull(req.getMemory());assertTrue(req.getHistory().isEmpty());return new AiIntentDto(AiIntentDto.Intent.SEATS,null,null,null,4,null);});when(seats.recommend(any())).thenReturn(List.of());assertEquals(4,service.chat(req).memory().people());
 }
}
