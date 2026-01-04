package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.ShowTime;
import com.cinema.ticketbooking.domain.Film;
import com.cinema.ticketbooking.domain.Auditorium;
import com.cinema.ticketbooking.domain.request.ReqCreateShowTimeDto;
import com.cinema.ticketbooking.domain.request.ReqUpdateShowTimeDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.repository.AuditoriumRepository;
import com.cinema.ticketbooking.repository.BookingItemRepository;
import com.cinema.ticketbooking.repository.FilmRepository;
import com.cinema.ticketbooking.repository.ShowTimeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ShowTimeService {
    private final ShowTimeRepository showTimeRepository;
    private final FilmRepository filmRepository;
    private final AuditoriumRepository auditoriumRepository;
    private final BookingItemRepository bookingItemRepository;

    public ShowTimeService(ShowTimeRepository showTimeRepository, FilmRepository filmRepository,
            AuditoriumRepository auditoriumRepository, BookingItemRepository bookingItemRepository) {
        this.showTimeRepository = showTimeRepository;
        this.filmRepository = filmRepository;
        this.auditoriumRepository = auditoriumRepository;
        this.bookingItemRepository = bookingItemRepository;
    }

    public ResultPaginationDto getAllShowTimes(Specification<ShowTime> spec, Pageable pageable) {
        Page<ShowTime> pageShowTime = this.showTimeRepository.findAll(spec, pageable);
        ResultPaginationDto resultPaginationDto = new ResultPaginationDto();
        ResultPaginationDto.Meta mt = new ResultPaginationDto.Meta();

        mt.setCurrentPage(pageable.getPageNumber() + 1);
        mt.setTotalPages(pageShowTime.getTotalPages());
        mt.setPageSize(pageable.getPageSize());
        mt.setTotalItems(pageShowTime.getTotalElements());

        resultPaginationDto.setMeta(mt);
        resultPaginationDto.setData(pageShowTime.getContent());

        return resultPaginationDto;
    }

    public ShowTime findShowTimeById(Long id) {
        return this.showTimeRepository.findById(id).orElse(null);
    }

    public ShowTime createShowTime(ReqCreateShowTimeDto reqShowTime) {
        ShowTime showTime = new ShowTime();
        Optional<Auditorium> auditorium = this.auditoriumRepository.findById(reqShowTime.getAuditoriumId());
        showTime.setAuditorium(auditorium.orElse(null));
        Optional<Film> film = this.filmRepository.findById(reqShowTime.getFilmId());
        showTime.setFilm(film.orElse(null));

        showTime.setDate(reqShowTime.getDate());
        showTime.setStartTime(reqShowTime.getStartTime());
        showTime.setEndTime(reqShowTime.getEndTime());

        this.showTimeRepository.save(showTime);
        return showTime;
    }

    public void deleteShowTime(Long id) {
        this.showTimeRepository.deleteById(id);
    }

    /**
     * Kiểm tra xem showtime có booking nào không
     */
    public boolean hasBookings(Long showTimeId) {
        return this.bookingItemRepository.existsByShowTimeId(showTimeId);
    }

    public ShowTime updateShowTime(ReqUpdateShowTimeDto reqShowTime) {
        ShowTime showTime = findShowTimeById(reqShowTime.getId());
        if (showTime == null)
            return null;

        showTime.setDate(reqShowTime.getDate());
        showTime.setStartTime(reqShowTime.getStartTime());
        showTime.setEndTime(reqShowTime.getEndTime());

        return this.showTimeRepository.save(showTime);
    }

}
