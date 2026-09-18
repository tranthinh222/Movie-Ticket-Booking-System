package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.*;
import com.cinema.ticketbooking.domain.request.ReqMovieRecommendationDto;
import com.cinema.ticketbooking.repository.ShowTimeRepository;
import com.cinema.ticketbooking.util.error.BadRequestException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class MovieRecommendationServiceTest {
    @Autowired
    TestEntityManager em;
    @Autowired
    ShowTimeRepository repository;
    final DiscountService discounts=org.mockito.Mockito.mock(DiscountService.class);
    final com.cinema.ticketbooking.repository.BookingItemRepository bookings=org.mockito.Mockito.mock(com.cinema.ticketbooking.repository.BookingItemRepository.class);
    final com.cinema.ticketbooking.repository.SeatHoldRepository holds=org.mockito.Mockito.mock(com.cinema.ticketbooking.repository.SeatHoldRepository.class);
    final LocalDate today = LocalDate.of(2026, 9, 18);

    MovieRecommendationService service() {
        return new MovieRecommendationService(repository, org.mockito.Mockito.mock(com.cinema.ticketbooking.repository.SeatRepository.class), discounts, bookings, holds,
                Clock.fixed(Instant.parse("2026-09-18T07:00:00Z"), ZoneId.of("Asia/Ho_Chi_Minh")));
    }

    Film film(String name, String genre, long duration) {
        var f = new Film();
        f.setName(name);
        f.setGenre(genre);
        f.setDuration(duration);
        em.persist(f);
        return f;
    }

    void show(Film f, LocalDate date, int hour) {
        var s = new ShowTime();
        s.setFilm(f);
        s.setDate(date);
        s.setStartTime(LocalTime.of(hour, 0));
        em.persist(s);
        em.flush();
    }

    @Test
    void filtersFutureShowsGenreDurationAndDuplicates() {
        var good = film("Good", "Gia đình, Hài, Tâm Lý", 112);
        show(good, today, 15);
        show(good, today, 17);
        show(film("Past", "Hài", 100), today, 13);
        show(film("Starting now", "Hài", 100), today, 14);
        show(film("Tomorrow", "Hài", 100), today.plusDays(1), 15);
        show(film("Long", "Hài", 130), today, 16);
        show(film("Wrong genre", "Hài hước", 100), today, 16);
        var req = new ReqMovieRecommendationDto();
        req.setDate(today);req.setGenre(" hài ");
        req.setMaxDuration(120);
        var result = service().recommend(req);
        assertEquals(1, result.size());
        assertEquals(good.getId(), result.get(0).filmId());
        assertTrue(result.get(0).reason().contains("18/09/2026"));
    }

    @Test
    void returnsEmptyAndRejectsPastDate() {
        var req = new ReqMovieRecommendationDto();
        assertTrue(service().recommend(req).isEmpty());
        req.setDate(today.minusDays(1));
        assertThrows(BadRequestException.class, () -> service().recommend(req));
    }

    @Test
    void limitsToFiveAndAcceptsFutureDate() {
        for (int i = 0; i < 7; i++)
            show(film("Movie" + i, "Hài", 100), today.plusDays(1), 10 + i);
        var req = new ReqMovieRecommendationDto();
        req.setDate(today.plusDays(1));
        assertEquals(5, service().recommend(req).size());
    }
 @Test void emptyDateFindsFutureShowsAfterTodaysShowsEnd(){
  var f=film("OPPENHEIMER","Tiểu Sử, Chính Kịch, Lịch Sử",180);
  show(f,today,13);show(f,today.plusDays(1),9);
  var req=new ReqMovieRecommendationDto();req.setGenre("Lịch sử");req.setMaxDuration(300);
  var result=service().recommend(req);assertEquals(1,result.size());assertEquals(f.getId(),result.get(0).filmId());assertTrue(result.get(0).reason().contains("19/09/2026"));
  req.setDate(today);assertTrue(service().recommend(req).isEmpty());
 }
 @Test void excludedMoviesDoNotConsumeResultLimit(){
  var previous=film("Previous","Hài",100);show(previous,today.plusDays(1),9);
  var next=film("Next","Hài",100);show(next,today.plusDays(1),10);
  var result=service().recommend(new ReqMovieRecommendationDto(),java.util.Set.of(previous.getId()));assertEquals(1,result.size());assertEquals(next.getId(),result.get(0).filmId());
 }

 @Test void budgetIncludesSeatPriceAndUnknownPricesAreExcluded(){
  var f=film("Budget film","Hài",100);f.setPrice(70000L);
  var room=new Auditorium();em.persist(room);
  var show=new ShowTime();show.setFilm(f);show.setAuditorium(room);show.setDate(today.plusDays(1));show.setStartTime(LocalTime.NOON);em.persist(show);em.flush();
  var seatRepo=org.mockito.Mockito.mock(com.cinema.ticketbooking.repository.SeatRepository.class);
  var seat=new Seat();seat.setId(99L);var variant=new SeatVariant();variant.setBasePrice(20000);variant.setBonus(10000);seat.setSeatVariant(variant);
  org.mockito.Mockito.when(seatRepo.findByAuditoriumId(room.getId())).thenReturn(java.util.List.of(seat));
  var service=new MovieRecommendationService(repository,seatRepo,discounts,bookings,holds,Clock.fixed(Instant.parse("2026-09-18T07:00:00Z"),ZoneId.of("Asia/Ho_Chi_Minh")));
  var req=new ReqMovieRecommendationDto();req.setBudget(new java.math.BigDecimal("100000"));
  assertEquals(new java.math.BigDecimal("100000.0"),service.recommend(req).get(0).minTicketPrice());
  req.setBudget(new java.math.BigDecimal("99999"));assertTrue(service.recommend(req).isEmpty());
  var offer=new Discount();offer.setCode("SAVE");offer.setMinOrder(new java.math.BigDecimal("100000"));
  org.mockito.Mockito.when(discounts.list(false)).thenReturn(java.util.List.of(offer));
  org.mockito.Mockito.when(discounts.calculate("SAVE",new java.math.BigDecimal("100000.0"))).thenReturn(new DiscountService.Quote("SAVE",new java.math.BigDecimal("100000.0"),new java.math.BigDecimal("20000"),new java.math.BigDecimal("80000")));
  req.setBudget(new java.math.BigDecimal("80000"));var result=service.recommend(req).get(0);assertEquals("SAVE",result.discountCode());assertEquals(new java.math.BigDecimal("80000"),result.finalTicketPrice());
  req.setBudget(new java.math.BigDecimal("79999"));assertTrue(service.recommend(req).isEmpty());
  req.setBudget(new java.math.BigDecimal("150000"));
  org.mockito.Mockito.when(bookings.findUnavailableSeatsForShows(org.mockito.ArgumentMatchers.anyList())).thenReturn(java.util.Collections.singletonList(new Object[]{show.getId(),99L}));
  assertTrue(service.recommend(req).isEmpty());
  org.mockito.Mockito.when(bookings.findUnavailableSeatsForShows(org.mockito.ArgumentMatchers.anyList())).thenReturn(java.util.List.of());
  org.mockito.Mockito.when(holds.findUnavailableSeatsForShows(org.mockito.ArgumentMatchers.anyList(),org.mockito.ArgumentMatchers.any())).thenReturn(java.util.Collections.singletonList(new Object[]{show.getId(),99L}));
  assertTrue(service.recommend(req).isEmpty());

  org.mockito.Mockito.when(seatRepo.findByAuditoriumId(room.getId())).thenReturn(java.util.List.of());assertTrue(service.recommend(req).isEmpty());
 }
}
