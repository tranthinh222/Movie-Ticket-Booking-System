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
    private final DiscountService discounts;
    private final com.cinema.ticketbooking.repository.BookingItemRepository bookings;
    private final com.cinema.ticketbooking.repository.SeatHoldRepository holds;
    private final com.cinema.ticketbooking.repository.SeatRepository seats;

    @org.springframework.beans.factory.annotation.Autowired
    public MovieRecommendationService(ShowTimeRepository repository,
            com.cinema.ticketbooking.repository.SeatRepository seats, DiscountService discounts,
            com.cinema.ticketbooking.repository.BookingItemRepository bookings,
            com.cinema.ticketbooking.repository.SeatHoldRepository holds) {
        this(repository, seats, discounts, bookings, holds, Clock.system(ZoneId.of("Asia/Ho_Chi_Minh")));
    }

    MovieRecommendationService(ShowTimeRepository repository, com.cinema.ticketbooking.repository.SeatRepository seats,
            DiscountService discounts, com.cinema.ticketbooking.repository.BookingItemRepository bookings,
            com.cinema.ticketbooking.repository.SeatHoldRepository holds, Clock clock) {
        this.repository = repository;
        this.clock = clock;
        this.seats = seats;
        this.discounts = discounts;
        this.bookings = bookings;
        this.holds = holds;
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
        var offers = request.getBudget() == null ? List.<com.cinema.ticketbooking.domain.Discount>of()
                : discounts.list(false);
        Map<Long, List<com.cinema.ticketbooking.domain.Seat>> roomSeats = new HashMap<>();
        Map<String, DiscountService.Quote> quotes = new HashMap<>();
        Map<Long, ResMovieRecommendationDto> results = new LinkedHashMap<>();
        var shows=repository.findRecommendationCandidates(date, now.toLocalDate(), now.toLocalTime());
        Map<Long, Set<Long>> unavailableByShow=new HashMap<>();
        var ids=shows.stream().map(com.cinema.ticketbooking.domain.ShowTime::getId).toList();
        if(!ids.isEmpty()){
            var blocked=new ArrayList<>(bookings.findUnavailableSeatsForShows(ids));
            blocked.addAll(holds.findUnavailableSeatsForShows(ids,now.atZone(clock.getZone()).toInstant()));
            for(var row:blocked)unavailableByShow.computeIfAbsent((Long)row[0],key->new HashSet<>()).add((Long)row[1]);
        }
        for (var show : shows) {
            var film = show.getFilm();
            if (film == null || film.getId() == null || excluded.contains(film.getId()) || results.containsKey(film.getId()))
                continue;
            if (!genre.isEmpty() && (film.getGenre() == null || Arrays.stream(film.getGenre().split("[,;]"))
                    .noneMatch(value -> value.trim().equalsIgnoreCase(genre))))
                continue;
            if (request.getMaxDuration() != null
                    && (film.getDuration() == null || film.getDuration() > request.getMaxDuration()))
                continue;
            java.math.BigDecimal price = null, finalPrice = null, saving = java.math.BigDecimal.ZERO;
            String discountCode = null;
            if (film.getPrice() != null && show.getAuditorium() != null) {
                var unavailable = unavailableByShow.getOrDefault(show.getId(),Set.of());
                var prices = roomSeats.computeIfAbsent(show.getAuditorium().getId(), seats::findByAuditoriumId).stream()
                        .filter(seat -> seat.getSeatVariant() != null && !unavailable.contains(seat.getId()))
                        .map(seat -> java.math.BigDecimal.valueOf(film.getPrice())
                                .add(java.math.BigDecimal.valueOf(seat.getSeatVariant().getBasePrice()))
                                .add(java.math.BigDecimal.valueOf(seat.getSeatVariant().getBonus())))
                        .distinct().sorted().toList();
                for (var candidate : prices) {
                    var total = candidate;
                    String code = null;
                    var amount = java.math.BigDecimal.ZERO;
                    for (var offer : offers) {
                        if (candidate.signum() <= 0 || candidate.compareTo(offer.getMinOrder()) < 0)
                            continue;
                        DiscountService.Quote quote;
                        try {
                            quote = quotes.computeIfAbsent(offer.getCode()+":"+candidate.toPlainString(),key->discounts.calculate(offer.getCode(), candidate));
                        } catch (BadRequestException changedOffer) {
                            continue;
                        }
                        if (quote.total().compareTo(total) < 0) {
                            total = quote.total();
                            code = quote.code();
                            amount = quote.discountAmount();
                        }
                    }
                    if (finalPrice == null || total.compareTo(finalPrice) < 0) {
                        price = candidate;
                        finalPrice = total;
                        discountCode = code;
                        saving = amount;
                    }
                }
            }
            if (request.getBudget() != null && (finalPrice == null || finalPrice.compareTo(request.getBudget()) > 0))
                continue;
            String reason = "Có suất chiếu ngày " + show.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    + ".";
            if (!genre.isEmpty())
                reason += " Phù hợp thể loại " + genre + ".";
            if (film.getDuration() != null)
                reason += " Thời lượng " + film.getDuration() + " phút.";
            reason += " Giá tính cho một vé theo loại ghế còn trống tại thời điểm tra cứu. Mỗi đơn dùng một mã; giá và điều kiện được kiểm tra lại khi thanh toán.";
            var suggestion = new ResMovieRecommendationDto(film.getId(), film.getName(),
                    film.getThumbnail(), film.getDuration(), film.getGenre(), reason, price, discountCode, saving,
                    finalPrice);
            var previous = results.get(film.getId());
            if (previous == null || (finalPrice != null
                    && (previous.finalTicketPrice() == null || finalPrice.compareTo(previous.finalTicketPrice()) < 0)))
                results.put(film.getId(), suggestion);
        }
        return results.values().stream().limit(5).toList();
    }
}
