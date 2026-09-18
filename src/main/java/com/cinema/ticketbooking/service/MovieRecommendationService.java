package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.request.ReqMovieRecommendationDto;
import com.cinema.ticketbooking.domain.response.ResMovieRecommendationDto;
import com.cinema.ticketbooking.repository.ShowTimeRepository;
import com.cinema.ticketbooking.util.error.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class MovieRecommendationService {
    private final ShowTimeRepository repository;
    private final Clock clock;
    private final com.cinema.ticketbooking.repository.SeatRepository seats;

    @org.springframework.beans.factory.annotation.Autowired
    public MovieRecommendationService(ShowTimeRepository repository, com.cinema.ticketbooking.repository.SeatRepository seats) {
        this(repository, seats, Clock.system(ZoneId.of("Asia/Ho_Chi_Minh")));
    }

    MovieRecommendationService(ShowTimeRepository repository, com.cinema.ticketbooking.repository.SeatRepository seats, Clock clock) {
        this.repository = repository;
        this.clock = clock;
        this.seats = seats;
    }

    @Transactional(readOnly = true)
    public List<ResMovieRecommendationDto> recommend(ReqMovieRecommendationDto request) {
        return recommend(request, Set.of());
    }

    @Transactional(readOnly = true)
    public List<ResMovieRecommendationDto> recommend(ReqMovieRecommendationDto request, Set<Long> excluded) {
        var now = LocalDateTime.now(clock);
        var date = request.getDate();
        if (date != null && date.isBefore(now.toLocalDate()))
            throw new BadRequestException("Ngày xem phim không được ở quá khứ.");
        var genre = request.getGenre() == null ? "" : request.getGenre().trim();
        Map<Long, ResMovieRecommendationDto> results = new LinkedHashMap<>();
        for (var show : repository.findRecommendationCandidates(date, now.toLocalDate(), now.toLocalTime())) {
            var film = show.getFilm();
            if (film == null || film.getId() == null || excluded.contains(film.getId()))
                continue;
            if (!genre.isEmpty() && (film.getGenre() == null || Arrays.stream(film.getGenre().split("[,;]"))
                    .noneMatch(value -> value.trim().equalsIgnoreCase(genre))))
                continue;
            if (request.getMaxDuration() != null
                    && (film.getDuration() == null || film.getDuration() > request.getMaxDuration()))
                continue;
            java.math.BigDecimal price=null;
            if(film.getPrice()!=null && show.getAuditorium()!=null){
                price=seats.findByAuditoriumId(show.getAuditorium().getId()).stream()
                    .filter(seat->seat.getSeatVariant()!=null)
                    .map(seat->java.math.BigDecimal.valueOf(film.getPrice())
                        .add(java.math.BigDecimal.valueOf(seat.getSeatVariant().getBasePrice()))
                        .add(java.math.BigDecimal.valueOf(seat.getSeatVariant().getBonus())))
                    .min(java.math.BigDecimal::compareTo).orElse(null);
            }
            if(request.getBudget()!=null && (price==null || price.compareTo(request.getBudget())>0))continue;
            String reason = "Có suất chiếu ngày " + show.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ".";
            if (!genre.isEmpty())
                reason += " Phù hợp thể loại " + genre + ".";
            if (film.getDuration() != null)
                reason += " Thời lượng " + film.getDuration() + " phút.";
            reason += " Giá chưa áp dụng ưu đãi; tùy loại ghế. Ghế ở mức giá này có thể đã được đặt.";
            results.putIfAbsent(film.getId(), new ResMovieRecommendationDto(film.getId(), film.getName(),
                    film.getThumbnail(), film.getDuration(), film.getGenre(), reason, price));
            if (results.size() == 5)
                break;
        }
        return List.copyOf(results.values());
    }
}
