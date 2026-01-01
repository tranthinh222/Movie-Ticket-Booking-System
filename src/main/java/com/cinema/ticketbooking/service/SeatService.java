package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.Seat;
import com.cinema.ticketbooking.domain.SeatVariant;
import com.cinema.ticketbooking.domain.Auditorium;
import com.cinema.ticketbooking.domain.request.ReqCreateSeatDto;
import com.cinema.ticketbooking.domain.request.ReqUpdateSeatDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.repository.AuditoriumRepository;
import com.cinema.ticketbooking.repository.SeatRepository;
import com.cinema.ticketbooking.repository.SeatVariantRepository;
import com.cinema.ticketbooking.util.constant.SeatStatusEnum;
import com.cinema.ticketbooking.util.constant.SeatTypeEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SeatService {
    private final SeatRepository seatRepository;
    private final AuditoriumRepository auditoriumRepository;
    private final SeatVariantRepository seatVariantRepository;

    public SeatService(SeatRepository seatRepository, AuditoriumRepository auditoriumRepository,
            SeatVariantRepository seatVariantRepository) {
        this.seatRepository = seatRepository;
        this.auditoriumRepository = auditoriumRepository;
        this.seatVariantRepository = seatVariantRepository;
    }

    public ResultPaginationDto getAllSeats(Specification<Seat> spec, Pageable pageable) {
        Page<Seat> pageSeat = this.seatRepository.findAll(spec, pageable);
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
        Optional<SeatVariant> seatVariant = this.seatVariantRepository.findById(reqSeat.getSeatVariantId());
        seat.setSeatVariant(seatVariant.orElse(null));
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

    public List<Seat> createDefaultSeatsForAuditorium(Auditorium auditorium) {

        Optional<SeatVariant> normalVariant = this.seatVariantRepository.findBySeatType(SeatTypeEnum.REG);
        if (!normalVariant.isPresent()) {
            throw new IllegalStateException("Seat type REG did not find");
        }

        Optional<SeatVariant> vipVariant = this.seatVariantRepository.findBySeatType(SeatTypeEnum.VIP);
        if (!vipVariant.isPresent()) {
            throw new IllegalStateException("Seat type VIP did not find");
        }

        List<Seat> seats = new ArrayList<>();
        char[] rows = { 'A', 'B', 'C', 'D', 'E', 'F' };

        for (char row : rows) {
            for (int number = 1; number <= 8; number++) {

                Seat seat = new Seat();
                seat.setAuditorium(auditorium);
                seat.setSeatRow(String.valueOf(row));
                seat.setNumber(number);
                seat.setStatus(SeatStatusEnum.AVAILABLE);

                seat.setSeatVariant(row == 'F' ? vipVariant.get() : normalVariant.get());

                seats.add(seat);
            }
        }

        seatRepository.saveAll(seats);
        return seats;
    }

}
