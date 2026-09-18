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

    @org.springframework.beans.factory.annotation.Autowired
    public MovieRecommendationService(ShowTimeRepository repository) {
        this(repository, Clock.system(ZoneId.of("Asia/Ho_Chi_Minh")));
    }

    MovieRecommendationService(ShowTimeRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<ResMovieRecommendationDto> recommend(ReqMovieRecommendationDto request) {
        var now = LocalDateTime.now(clock);
        var date = request.getDate() == null ? now.toLocalDate() : request.getDate();
        if (date.isBefore(now.toLocalDate()))
            throw new BadRequestException("Ngày xem phim không được ở quá khứ.");
        var genre = request.getGenre() == null ? "" : request.getGenre().trim();
        Map<Long, ResMovieRecommendationDto> results = new LinkedHashMap<>();
        for (var show : repository.findRecommendationCandidates(date, now.toLocalDate(), now.toLocalTime())) {
            var film = show.getFilm();
            if (film == null || film.getId() == null)
                continue;
            if (!genre.isEmpty() && (film.getGenre() == null || Arrays.stream(film.getGenre().split("[,;]"))
                    .noneMatch(value -> value.trim().equalsIgnoreCase(genre))))
                continue;
            if (request.getMaxDuration() != null
                    && (film.getDuration() == null || film.getDuration() > request.getMaxDuration()))
                continue;
            String reason = "Có suất chiếu ngày " + date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + ".";
            if (!genre.isEmpty())
                reason += " Phù hợp thể loại " + genre + ".";
            if (film.getDuration() != null)
                reason += " Thời lượng " + film.getDuration() + " phút.";
            results.putIfAbsent(film.getId(), new ResMovieRecommendationDto(film.getId(), film.getName(),
                    film.getThumbnail(), film.getDuration(), film.getGenre(), reason));
            if (results.size() == 5)
                break;
        }
        return List.copyOf(results.values());
    }
}
