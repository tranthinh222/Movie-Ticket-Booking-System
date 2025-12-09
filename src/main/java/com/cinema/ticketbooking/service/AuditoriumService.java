package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.Auditorium;
import com.cinema.ticketbooking.domain.Theater;
import com.cinema.ticketbooking.domain.request.ReqCreateAuditoriumDto;
import com.cinema.ticketbooking.domain.request.ReqUpdateAuditoriumDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.repository.AuditoriumRepository;
import com.cinema.ticketbooking.repository.TheaterRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuditoriumService {
    private final AuditoriumRepository auditoriumRepository;
    private final TheaterRepository theaterRepository;

    public AuditoriumService(AuditoriumRepository auditoriumRepository, TheaterRepository theaterRepository) {
        this.auditoriumRepository = auditoriumRepository;
        this.theaterRepository = theaterRepository;
    }

    public ResultPaginationDto getAllAuditoriums(Specification<Auditorium> spec, Pageable pageable) {
        Page<Auditorium> pageAuditorium = this.auditoriumRepository.findAll(spec, pageable);
        ResultPaginationDto resultPaginationDto = new ResultPaginationDto();
        ResultPaginationDto.Meta mt = new ResultPaginationDto.Meta();

        mt.setCurrentPage(pageable.getPageNumber() + 1);
        mt.setTotalPages(pageAuditorium.getTotalPages());
        mt.setPageSize(pageable.getPageSize());
        mt.setTotalItems(pageAuditorium.getTotalElements());

        resultPaginationDto.setMeta(mt);
        resultPaginationDto.setData(pageAuditorium.getContent());

        return resultPaginationDto;
    }

    public Auditorium createAuditorium(ReqCreateAuditoriumDto reqAuditorium) {
        Auditorium auditorium = new Auditorium();
        auditorium.setNumber(reqAuditorium.getNumber());
        auditorium.setTotalSeats(reqAuditorium.getTotalSeats());
        Optional<Theater> theater = this.theaterRepository.findById(reqAuditorium.getTheaterId());
        auditorium.setTheater(theater.orElse(null));

        this.auditoriumRepository.save(auditorium);
        return auditorium;
    }

    public Auditorium getAuditoriumById(Long auditoriumId) {
        return this.auditoriumRepository.findById(auditoriumId).orElse(null);
    }

    public void deleteAuditorium(Long auditoriumId) {
        this.auditoriumRepository.deleteById(auditoriumId);
    }

    public Auditorium updateAuditorium(ReqUpdateAuditoriumDto reqUpdateAuditorium) {
        Auditorium auditorium = new Auditorium();
        auditorium.setNumber(reqUpdateAuditorium.getNumber());
        auditorium.setTotalSeats(reqUpdateAuditorium.getTotalSeat());
        return auditoriumRepository.save(auditorium);
    }

}
