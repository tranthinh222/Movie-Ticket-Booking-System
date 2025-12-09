package com.cinema.ticketbooking.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cinema.ticketbooking.domain.Seat;
import com.cinema.ticketbooking.domain.SeatHold;
import com.cinema.ticketbooking.domain.ShowTime;
import com.cinema.ticketbooking.domain.User;
import com.cinema.ticketbooking.domain.request.ReqCreateSeatHoldDto;
import com.cinema.ticketbooking.repository.SeatHoldRepository;
import com.cinema.ticketbooking.repository.SeatRepository;
import com.cinema.ticketbooking.repository.ShowTimeRepository;
import com.cinema.ticketbooking.repository.UserRepository;
import com.cinema.ticketbooking.util.SecurityUtil;
import com.cinema.ticketbooking.util.constant.SeatStatusEnum;

import jakarta.transaction.Transactional;

@Service
public class SeatHoldService {
    private final SeatHoldRepository seatHoldRepository;
    private final UserRepository userRepository;
    private final ShowTimeRepository showTimeRepository;
    private final SeatRepository seatRepository;

    public SeatHoldService(SeatHoldRepository seatHoldRepository, UserRepository userRepository,
            ShowTimeRepository showTimeRepository, SeatRepository seatRepository, SeatService seatService) {
        this.seatHoldRepository = seatHoldRepository;
        this.userRepository = userRepository;
        this.showTimeRepository = showTimeRepository;
        this.seatRepository = seatRepository;
    }

    @Transactional
    public SeatHold createSeatHold(ReqCreateSeatHoldDto reqSeatHold) {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("Unauthenticated"));

        User currentUserDB = this.userRepository.findUserByEmail(email);

        // Lock seat để tránh race condition (giả sử lockSeat là custom method với
        // pessimistic lock)
        // Seat seat = this.seatRepository.lockSeat(reqSeatHold.getSeatId());
        Optional<Seat> seat = this.seatRepository.findById(reqSeatHold.getSeatId());

        // Set HOLD (Controller đã check AVAILABLE, nhưng lock để an toàn)
        seat.get().setStatus(SeatStatusEnum.HOLD);
        this.seatRepository.save(seat.get());

        ShowTime showtime = this.showTimeRepository
                .findById(reqSeatHold.getShowtimeId())
                .orElseThrow(() -> new RuntimeException("ShowTime not found"));

        SeatHold seatHold = new SeatHold();
        seatHold.setSeat(seat.get());
        seatHold.setShowTime(showtime);
        seatHold.setUser(currentUserDB);

        seatHold.setExpiresAt(Instant.now().plus(10, ChronoUnit.MINUTES));

        this.seatHoldRepository.save(seatHold);

        return seatHold;
    }

    public List<SeatHold> getSeatHoldByUserId(Long id) {
        List<SeatHold> listItem = this.seatHoldRepository.findByUserIdFetchFull(id);
        return listItem;
    }
}
