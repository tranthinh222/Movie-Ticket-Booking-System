package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.*;
import com.cinema.ticketbooking.domain.request.ReqSeatRecommendationDto;
import com.cinema.ticketbooking.repository.*;
import com.cinema.ticketbooking.util.error.BadRequestException;
import org.junit.jupiter.api.Test;
import java.util.*;
import java.time.*;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SeatRecommendationServiceTest {
    ShowTimeRepository shows = mock(ShowTimeRepository.class);
    SeatRepository seats = mock(SeatRepository.class);
    BookingItemRepository bookings = mock(BookingItemRepository.class);
    SeatHoldRepository holds = mock(SeatHoldRepository.class);
    SeatRecommendationService service = new SeatRecommendationService(shows, seats, bookings, holds);

    ShowTime setup() {
        var show = new ShowTime();
        show.setId(1L);
        show.setDate(LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).plusDays(1));
        show.setStartTime(LocalTime.NOON);
        var room = new Auditorium();
        room.setId(2L);
        show.setAuditorium(room);
        var film = new Film();
        film.setPrice(70000L);
        show.setFilm(film);
        when(shows.findById(1L)).thenReturn(Optional.of(show));
        return show;
    }

    Seat seat(long id, int number) {
        var s = new Seat();
        s.setId(id);
        s.setSeatRow("A");
        s.setNumber(number);
        var v = new SeatVariant();
        v.setBasePrice(50000);
        v.setBonus(10000);
        s.setSeatVariant(v);
        return s;
    }

    ReqSeatRecommendationDto req() {
        var r = new ReqSeatRecommendationDto();
        r.setShowTimeId(1L);
        return r;
    }

    @Test
    void choosesConsecutiveSeatNumbersNotIdsAndComputesActualPrice() {
        setup();
        when(seats.findByAuditoriumId(2L)).thenReturn(List.of(seat(90, 3), seat(4, 1), seat(22, 2), seat(50, 4)));
        var result = service.recommend(req());
        assertEquals(List.of(4L, 22L), result.get(0).seatIds());
        assertEquals(0, new BigDecimal("260000").compareTo(result.get(0).totalPrice()));
        assertEquals(2, result.size());
        assertTrue(result.stream().noneMatch(g -> g.seatIds().equals(List.of(22L, 90L))));
    }

    @Test
    void excludesBookedHeldGapsAndOverBudget() {
        setup();
        when(seats.findByAuditoriumId(2L)).thenReturn(List.of(seat(1, 1), seat(2, 2), seat(4, 4), seat(5, 5)));
        when(bookings.findUnavailableSeatIds(1L)).thenReturn(List.of(2L));
        when(holds.findUnavailableSeatIds(eq(1L), any())).thenReturn(List.of(4L));
        assertTrue(service.recommend(req()).isEmpty());
        when(bookings.findUnavailableSeatIds(1L)).thenReturn(List.of());
        when(holds.findUnavailableSeatIds(eq(1L), any())).thenReturn(List.of());
        var r = req();
        r.setBudget(new BigDecimal("259999"));
        assertTrue(service.recommend(r).isEmpty());
    }

    @Test
    void rejectsMissingOrPastShow() {
        assertThrows(BadRequestException.class, () -> service.recommend(req()));
        var s = setup();
        s.setDate(LocalDate.now().minusDays(2));
        assertThrows(BadRequestException.class, () -> service.recommend(req()));
    }
}
