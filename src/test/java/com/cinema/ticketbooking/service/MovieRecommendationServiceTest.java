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
    final LocalDate today = LocalDate.of(2026, 9, 18);

    MovieRecommendationService service() {
        return new MovieRecommendationService(repository,
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
}
