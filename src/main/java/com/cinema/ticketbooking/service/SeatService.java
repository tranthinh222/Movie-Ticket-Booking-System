package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.Seat;
import com.cinema.ticketbooking.domain.Auditorium;
import com.cinema.ticketbooking.domain.Seat;
import com.cinema.ticketbooking.domain.request.ReqCreateSeatDto;
import com.cinema.ticketbooking.domain.request.ReqUpdateSeatDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.repository.AuditoriumRepository;
import com.cinema.ticketbooking.repository.SeatRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SeatService {
    private final SeatRepository seatRepository;
    private final AuditoriumRepository auditoriumRepository;
    public SeatService(SeatRepository seatRepository, AuditoriumRepository auditoriumRepository){
        this.seatRepository = seatRepository;
        this.auditoriumRepository = auditoriumRepository;
    }

    public ResultPaginationDto getAllSeats(Specification<Seat> spec, Pageable pageable) {
        Page<Seat> pageSeat =  this.seatRepository.findAll(spec, pageable);
        ResultPaginationDto resultPaginationDto = new ResultPaginationDto();
        ResultPaginationDto.Meta mt = new ResultPaginationDto.Meta();

        mt.setCurrentPage(pageable.getPageNumber() + 1);
        mt.setTotalPages(pageSeat.getTotalPages());
        mt.setPageSize(pageable.getPageSize());
        mt.setTotalItems(pageSeat.getTotalElements());

        resultPaginationDto.setMeta(mt);
        resultPaginationDto.setData(pageSeat.getContent());

        return resultPaginationDto;
    }

    public Seat findSeatById(Long id) {
        return this.seatRepository.findById(id).orElse(null);
    }

    public void deleteSeat(Long id) {
        this.seatRepository.deleteById(id);
    }

    public Seat createSeat(ReqCreateSeatDto reqSeat) {
        Seat seat = new Seat();
        Optional<Auditorium> auditorium = this.auditoriumRepository.findById(reqSeat.getAuditoriumId());
        seat.setAuditorium(auditorium.orElse(null));
        seat.setSeatRow(reqSeat.getSeatRow());
        seat.setNumber(reqSeat.getNumber());
        seat.setStatus(reqSeat.getStatus());

        this.seatRepository.save(seat);
        return seat;
    }

    public Seat updateSeat(ReqUpdateSeatDto reqSeat) {
        Seat seat = findSeatById(reqSeat.getId());
        if (seat == null)
            return null;
        seat.setSeatRow(reqSeat.getSeatRow());
        seat.setNumber(reqSeat.getNumber());
        seat.setStatus(reqSeat.getStatus());
        this.seatRepository.save(seat);
        return seat;
    }
}
