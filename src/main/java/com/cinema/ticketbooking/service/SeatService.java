package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.Seat;
import com.cinema.ticketbooking.domain.SeatVariant;
import com.cinema.ticketbooking.domain.ShowTime;
import com.cinema.ticketbooking.domain.Auditorium;
import com.cinema.ticketbooking.domain.request.ReqCreateSeatDto;
import com.cinema.ticketbooking.domain.request.ReqUpdateSeatDto;
import com.cinema.ticketbooking.domain.response.ResSeatAvailabilityDto;
import com.cinema.ticketbooking.domain.response.ResSeatDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.repository.AuditoriumRepository;
import com.cinema.ticketbooking.repository.BookingItemRepository;
import com.cinema.ticketbooking.repository.SeatHoldRepository;
import com.cinema.ticketbooking.repository.SeatRepository;
import com.cinema.ticketbooking.repository.SeatVariantRepository;
import com.cinema.ticketbooking.repository.ShowTimeRepository;
import com.cinema.ticketbooking.util.constant.SeatStatusEnum;
import com.cinema.ticketbooking.util.constant.SeatTypeEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.time.Instant;

@Service
public class SeatService {
    private final SeatRepository seatRepository;
    private final AuditoriumRepository auditoriumRepository;
    private final SeatVariantRepository seatVariantRepository;
    private final SeatHoldRepository seatHoldRepository;
    private final BookingItemRepository bookingItemRepository;
    private final ShowTimeRepository showTimeRepository;

    public SeatService(SeatRepository seatRepository, AuditoriumRepository auditoriumRepository,
            SeatVariantRepository seatVariantRepository, SeatHoldRepository seatHoldRepository,
            BookingItemRepository bookingItemRepository, ShowTimeRepository showTimeRepository) {
        this.seatRepository = seatRepository;
        this.auditoriumRepository = auditoriumRepository;
        this.seatVariantRepository = seatVariantRepository;
        this.seatHoldRepository = seatHoldRepository;
        this.bookingItemRepository = bookingItemRepository;
        this.showTimeRepository = showTimeRepository;
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

    public List<ResSeatDto> findSeatsByAuditoriumId(Long auditoriumId) {
        if (!auditoriumRepository.existsById(auditoriumId)) {
            throw new com.cinema.ticketbooking.util.error.IdInvalidException(
                    "Auditorium with id " + auditoriumId + " not found");
        }
        return seatRepository.findByAuditoriumIdOrderBySeatRowAscNumberAsc(auditoriumId).stream()
                .map(seat -> {
                    ResSeatDto dto = new ResSeatDto();
                    dto.setId(seat.getId());
                    dto.setSeatRow(seat.getSeatRow());
                    dto.setNumber(seat.getNumber());
                    dto.setSeatVariantName(seat.getSeatVariant() == null
                            ? null
                            : seat.getSeatVariant().getSeatType().name());
                    return dto;
                })
                .toList();
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

        this.seatRepository.save(seat);
        return seat;
    }

    public Seat updateSeat(ReqUpdateSeatDto reqSeat) {
        Seat seat = findSeatById(reqSeat.getId());
        if (seat == null)
            return null;
        seat.setSeatRow(reqSeat.getSeatRow());
        seat.setNumber(reqSeat.getNumber());
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
                seat.setSeatVariant(row == 'F' ? vipVariant.get() : normalVariant.get());

                seats.add(seat);
            }
        }

        seatRepository.saveAll(seats);
        return seats;
    }

    /**
     * Lấy danh sách ghế với trạng thái cho một showtime cụ thể
     */
    public List<ResSeatAvailabilityDto> getSeatAvailabilityByShowTime(Long showTimeId) {
        ShowTime showTime = showTimeRepository.findById(showTimeId)
                .orElseThrow(() -> new RuntimeException("ShowTime not found"));

        if (showTime.getAuditorium() == null) {
            throw new IllegalStateException("ShowTime " + showTimeId + " has no auditorium");
        }

        List<Seat> seats = seatRepository
                .findByAuditoriumIdOrderBySeatRowAscNumberAsc(showTime.getAuditorium().getId());
        var bookedSeatIds = new HashSet<>(bookingItemRepository.findUnavailableSeatIds(showTimeId));
        var heldSeatIds = new HashSet<>(seatHoldRepository.findUnavailableSeatIds(showTimeId, Instant.now()));
        List<ResSeatAvailabilityDto> result = new ArrayList<>();

        for (Seat seat : seats) {
            ResSeatAvailabilityDto dto = new ResSeatAvailabilityDto();
            dto.setSeatId(seat.getId());
            dto.setSeatRow(seat.getSeatRow());
            dto.setNumber(seat.getNumber());

            // Set seat variant info
            if (seat.getSeatVariant() != null) {
                dto.setSeatVariantId(seat.getSeatVariant().getId());
                dto.setSeatVariantName(seat.getSeatVariant().getSeatType().name());
                double basePrice = Optional.ofNullable(seat.getSeatVariant().getBasePrice()).orElse(0D);
                double bonus = Optional.ofNullable(seat.getSeatVariant().getBonus()).orElse(0D);
                dto.setBasePrice(basePrice);
                dto.setBonus(bonus);
                dto.setTotalPrice(basePrice + bonus);
            }

            // Check status theo showtime
            if (bookedSeatIds.contains(seat.getId())) {
                dto.setStatus(SeatStatusEnum.BOOKED);
            } else {
                dto.setStatus(heldSeatIds.contains(seat.getId()) ? SeatStatusEnum.HOLD : SeatStatusEnum.AVAILABLE);
            }

            result.add(dto);
        }

        return result;
    }

}
