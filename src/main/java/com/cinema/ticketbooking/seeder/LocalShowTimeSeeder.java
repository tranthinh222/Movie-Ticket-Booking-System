package com.cinema.ticketbooking.seeder;

import com.cinema.ticketbooking.domain.ShowTime;
import com.cinema.ticketbooking.repository.AuditoriumRepository;
import com.cinema.ticketbooking.repository.FilmRepository;
import com.cinema.ticketbooking.repository.ShowTimeRepository;
import com.cinema.ticketbooking.util.constant.FilmStatusEnum;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Comparator;

/** Keeps a rolling seven-day schedule without deleting showtimes used by bookings. */
@Component
@Order(6)
public class LocalShowTimeSeeder implements CommandLineRunner {
    private final FilmRepository films;
    private final AuditoriumRepository rooms;
    private final ShowTimeRepository showtimes;

    public LocalShowTimeSeeder(FilmRepository films, AuditoriumRepository rooms,
                              ShowTimeRepository showtimes) {
        this.films = films;
        this.rooms = rooms;
        this.showtimes = showtimes;
    }

    @Override
    @Transactional
    public void run(String... args) {
        refreshWeeklySchedule();
    }

    // Refresh every Wednesday at 02:05 and on application startup.
    @Scheduled(cron = "0 5 2 * * WED", zone = "Asia/Ho_Chi_Minh")
    @Transactional
    public void refreshWeeklySchedule() {
        var zone = ZoneId.of("Asia/Ho_Chi_Minh");
        var today = LocalDate.now(zone);

        // Booking items reference showtimes, so only expired, unused rows are safe to remove.
        var expired = showtimes.findExpiredWithoutBookings(today);
        showtimes.deleteAll(expired);

        var availableFilms = films.findAll().stream()
                .filter(f -> f.getStatus() == FilmStatusEnum.NOW_SHOWING)
                .filter(f -> f.getDuration() != null && f.getDuration() > 0)
                .sorted(Comparator.comparing(f -> f.getId())).toList();
        var availableRooms = rooms.findAll().stream()
                .sorted(Comparator.comparing(r -> r.getId())).toList();
        if (availableFilms.isEmpty() || availableRooms.isEmpty()) {
            return;
        }
        var existing = showtimes.findAllByDateBetween(today, today.plusDays(6));
        int created = 0;
        for (int day = 0; day < 7; day++) {
            var date = today.plusDays(day);
            for (int roomIndex = 0; roomIndex < availableRooms.size(); roomIndex++) {
                var room = availableRooms.get(roomIndex);
                int slot = 0;
                long startMinute = 9 * 60;
                while (startMinute < 23 * 60) {
                    var film = availableFilms.get((day + roomIndex + slot * availableRooms.size())
                            % availableFilms.size());
                    long endMinute = startMinute + film.getDuration();
                    if (endMinute >= 24 * 60) {
                        break;
                    }
                    var start = LocalTime.ofSecondOfDay(startMinute * 60);
                    var end = LocalTime.ofSecondOfDay(endMinute * 60);
                    boolean conflict = existing.stream().anyMatch(s ->
                            s.getAuditorium().getId().equals(room.getId()) && s.getDate().equals(date)
                                    && start.isBefore(s.getEndTime()) && end.isAfter(s.getStartTime()));
                    if (!conflict && (date.isAfter(today) || start.isAfter(LocalTime.now(zone)))) {
                        var schedule = new ShowTime();
                        schedule.setFilm(film);
                        schedule.setAuditorium(room);
                        schedule.setDate(date);
                        schedule.setStartTime(start);
                        schedule.setEndTime(end);
                        existing.add(showtimes.save(schedule));
                        created++;
                    }
                    startMinute = endMinute + 20;
                    slot++;
                }
            }
        }
        System.out.println("Weekly schedule: removed " + expired.size() + " expired unused showtimes; added "
                + created + " showtimes for " + today + " to " + today.plusDays(6));
    }
}
