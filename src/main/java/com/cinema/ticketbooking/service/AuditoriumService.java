package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.Auditorium;
import com.cinema.ticketbooking.domain.Seat;
import com.cinema.ticketbooking.domain.SeatVariant;
import com.cinema.ticketbooking.domain.Theater;
import com.cinema.ticketbooking.domain.request.ReqCreateAuditoriumDto;
import com.cinema.ticketbooking.domain.request.ReqUpdateAuditoriumDto;
import com.cinema.ticketbooking.domain.response.ResAuditoriumDto;
import com.cinema.ticketbooking.domain.response.ResSeatDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.repository.AuditoriumRepository;
import com.cinema.ticketbooking.repository.TheaterRepository;
import com.cinema.ticketbooking.util.error.IdInvalidException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuditoriumService {
    private final AuditoriumRepository auditoriumRepository;
    private final TheaterRepository theaterRepository;
    private final SeatService seatService;

    public AuditoriumService(AuditoriumRepository auditoriumRepository, TheaterRepository theaterRepository,
            SeatService seatService) {
        this.auditoriumRepository = auditoriumRepository;
        this.theaterRepository = theaterRepository;
        this.seatService = seatService;
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

    @Transactional
    public Auditorium createAuditorium(ReqCreateAuditoriumDto reqAuditorium) {
        Theater theater = theaterRepository.findById(reqAuditorium.getTheaterId())
                .orElseThrow(() -> new RuntimeException("Theater not found"));

        Auditorium auditorium = new Auditorium();
        auditorium.setNumber(reqAuditorium.getNumber());
        auditorium.setTheater(theater);

        auditorium = auditoriumRepository.save(auditorium);

        List<Seat> listSeat = this.seatService.createDefaultSeatsForAuditorium(auditorium);
        auditorium.setTotalSeats((long) listSeat.size());
        auditorium = auditoriumRepository.save(auditorium);

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

    public ResSeatDto convertToSeatDto(Seat seat) {
        ResSeatDto dto = new ResSeatDto();
        dto.setId(seat.getId());
        dto.setSeatRow(seat.getSeatRow());
        dto.setNumber(seat.getNumber());
        dto.setSeatVariantName(seat.getSeatVariant() != null ? seat.getSeatVariant().getSeatType().name() : null);
        return dto;
    }

    public List<ResSeatDto> getSeatByAuditoriumId(Long auditoriumId) {
        Optional<Auditorium> auditorium = this.auditoriumRepository.findById(auditoriumId);
        if (!auditorium.isPresent()) {
            throw new IdInvalidException("Id auditorium with " + auditoriumId + " not found");
        }

        Auditorium a = auditorium.get();

        return a.getSeats()
                .stream()
                .map(this::convertToSeatDto)
                .collect(Collectors.toList());
    }
}
