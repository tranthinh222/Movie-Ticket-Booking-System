package com.cinema.ticketbooking.seeder;

import com.cinema.ticketbooking.domain.*;
import com.cinema.ticketbooking.repository.*;
import com.cinema.ticketbooking.util.constant.FilmStatusEnum;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LocalShowTimeSeederTest {
    @Test
    void createsUpcomingSchedulesWithoutDuplicatesOrMovingExistingSchedules() {
        var films = mock(FilmRepository.class);
        var rooms = mock(AuditoriumRepository.class);
        var schedules = mock(ShowTimeRepository.class);
        var film = new Film();
        film.setId(1L);
        film.setStatus(FilmStatusEnum.NOW_SHOWING);
        film.setDuration(148L);
        var room = new Auditorium();
        room.setId(1L);
        var today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        var original = new ShowTime();
        original.setAuditorium(room);
        original.setDate(today.plusDays(1));
        original.setStartTime(LocalTime.of(9, 0));
        original.setEndTime(LocalTime.of(12, 0));
        var stored = new ArrayList<ShowTime>(List.of(original));
        when(films.findAll()).thenReturn(List.of(film));
        when(rooms.findAll()).thenReturn(List.of(room));
        when(schedules.findAllByDateBetween(any(), any()))
                .thenAnswer(invocation -> new ArrayList<>(stored));
        when(schedules.save(any())).thenAnswer(invocation -> {
            ShowTime schedule = invocation.getArgument(0);
            stored.add(schedule);
            return schedule;
        });
        var seeder = new LocalShowTimeSeeder(films, rooms, schedules);
        seeder.run();
        int count = stored.size();
        assertTrue(count > 1);
        assertTrue(stored.stream().anyMatch(s -> s.getDate().equals(today.plusDays(6))));
        for (int i = 0; i < stored.size(); i++) {
            var a = stored.get(i);
            assertFalse(a.getDate().isBefore(today));
            assertTrue(a.getStartTime().isBefore(a.getEndTime()));
            for (int j = i + 1; j < stored.size(); j++) {
                var b = stored.get(j);
                if (a.getDate().equals(b.getDate())) {
                    assertFalse(a.getStartTime().isBefore(b.getEndTime())
                            && a.getEndTime().isAfter(b.getStartTime()));
                }
            }
        }
        seeder.run();
        assertEquals(count, stored.size());
        assertEquals(LocalTime.of(9, 0), original.getStartTime());
        assertEquals(LocalTime.of(12, 0), original.getEndTime());
    }
}
