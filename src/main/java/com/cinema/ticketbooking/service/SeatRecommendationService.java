package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.Seat;
import com.cinema.ticketbooking.domain.request.ReqSeatRecommendationDto;
import com.cinema.ticketbooking.domain.response.ResSeatRecommendationDto;
import com.cinema.ticketbooking.repository.*;
import com.cinema.ticketbooking.util.error.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;
import java.math.BigDecimal;

@Service
public class SeatRecommendationService {
    private final ShowTimeRepository shows;
    private final SeatRepository seats;
    private final BookingItemRepository bookings;
    private final SeatHoldRepository holds;

    public SeatRecommendationService(ShowTimeRepository shows, SeatRepository seats, BookingItemRepository bookings,
            SeatHoldRepository holds) {
        this.shows = shows;
        this.seats = seats;
        this.bookings = bookings;
        this.holds = holds;
    }

    private record Candidate(ResSeatRecommendationDto result, double distance) {
    }

    @Transactional(readOnly = true)
    public List<ResSeatRecommendationDto> recommend(ReqSeatRecommendationDto request) {
        var show = shows.findById(request.getShowTimeId())
                .orElseThrow(() -> new BadRequestException("Suất chiếu không tồn tại."));
        var now = Instant.now();
        var local = LocalDateTime.ofInstant(now, ZoneId.of("Asia/Ho_Chi_Minh"));
        if (show.getDate() == null || show.getStartTime() == null
                || !LocalDateTime.of(show.getDate(), show.getStartTime()).isAfter(local))
            throw new BadRequestException("Suất chiếu đã bắt đầu hoặc không còn đặt được.");
        if (show.getAuditorium() == null || show.getFilm() == null || show.getFilm().getPrice() == null)
            throw new BadRequestException("Suất chiếu chưa có đủ thông tin phòng và giá vé.");
        var unavailable = new HashSet<>(bookings.findUnavailableSeatIds(show.getId()));
        unavailable.addAll(holds.findUnavailableSeatIds(show.getId(), now));
        Map<String, List<Seat>> rows = new TreeMap<>();
        var roomSeats = seats.findByAuditoriumId(show.getAuditorium().getId());
        int aisleAfter = roomSeats.stream().mapToInt(Seat::getNumber).max().orElse(0) / 2;
        for (var seat : roomSeats)
            if (seat.getSeatRow() != null)
                rows.computeIfAbsent(seat.getSeatRow(), key -> new ArrayList<>()).add(seat);
        List<Candidate> candidates = new ArrayList<>();
        for (var row : rows.values()) {
            row.sort(Comparator.comparingInt(Seat::getNumber));
            double center = (row.get(0).getNumber() + row.get(row.size() - 1).getNumber()) / 2.0;
            for (int i = 0; i + request.getPeople() <= row.size(); i++) {
                var group = row.subList(i, i + request.getPeople());
                if (group.get(0).getNumber() <= aisleAfter && group.get(group.size() - 1).getNumber() > aisleAfter)
                    continue;
                boolean valid = true;
                BigDecimal total = BigDecimal.ZERO;
                for (int j = 0; j < group.size(); j++) {
                    var seat = group.get(j);
                    var variant = seat.getSeatVariant();
                    if (unavailable.contains(seat.getId()) || variant == null
                            || (j > 0 && seat.getNumber() != group.get(j - 1).getNumber() + 1)) {
                        valid = false;
                        break;
                    }
                    total = total.add(BigDecimal.valueOf(show.getFilm().getPrice()))
                            .add(BigDecimal.valueOf(variant.getBasePrice()))
                            .add(BigDecimal.valueOf(variant.getBonus()));
                }
                if (!valid || (request.getBudget() != null && total.compareTo(request.getBudget()) > 0))
                    continue;
                double distance = Math
                        .abs((group.get(0).getNumber() + group.get(group.size() - 1).getNumber()) / 2.0 - center);
                candidates.add(new Candidate(new ResSeatRecommendationDto(show.getId(),
                        group.stream().map(Seat::getId).toList(),
                        group.stream().map(s -> s.getSeatRow() + s.getNumber()).toList(), total,
                        "Ghế cùng hàng, số ghế liên tiếp và không qua lối đi giữa; ưu tiên vị trí gần giữa hàng. Giá chưa áp dụng mã ưu đãi."),
                        distance));
            }
        }
        return candidates
                .stream().sorted(Comparator.comparingDouble(Candidate::distance)
                        .thenComparing(c -> c.result().totalPrice()).thenComparing(c -> c.result().labels().get(0)))
                .limit(3).map(Candidate::result).toList();
    }
}
