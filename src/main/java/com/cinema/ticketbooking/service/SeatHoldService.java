package com.cinema.ticketbooking.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.cinema.ticketbooking.domain.Seat;
import com.cinema.ticketbooking.domain.SeatHold;
import com.cinema.ticketbooking.domain.ShowTime;
import com.cinema.ticketbooking.domain.User;
import com.cinema.ticketbooking.domain.request.ReqCreateSeatHoldDto;
import com.cinema.ticketbooking.domain.request.ReqRemoveSeatHold;
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
    public List<SeatHold> createSeatHold(ReqCreateSeatHoldDto req) {

        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("Unauthenticated"));

        User user = userRepository.findUserByEmail(email);

        ShowTime showtime = showTimeRepository.findById(req.getShowtimeId())
                .orElseThrow(() -> new RuntimeException("ShowTime not found"));

        List<Seat> seats = seatRepository.lockSeats(req.getSeatIds());

        if (seats.size() != req.getSeatIds().size()) {
            throw new RuntimeException("Some seats not found");
        }

        for (Seat seat : seats) {
            if (seat.getStatus() != SeatStatusEnum.AVAILABLE) {
                throw new RuntimeException("Seat " + seat.getId() + " is not available");
            }
        }

        Instant expiresAt = Instant.now().plus(5, ChronoUnit.MINUTES);

        List<SeatHold> seatHolds = new ArrayList<>();

        for (Seat seat : seats) {
            seat.setStatus(SeatStatusEnum.HOLD);

            SeatHold hold = new SeatHold();
            hold.setSeat(seat);
            hold.setShowTime(showtime);
            hold.setUser(user);
            hold.setExpiresAt(expiresAt);

            seatHolds.add(hold);
        }

        seatRepository.saveAll(seats);
        seatHoldRepository.saveAll(seatHolds);

        return seatHolds;
    }

    @Transactional
    public void removeSeatHold() {

        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("Unauthenticated"));

        User user = userRepository.findUserByEmail(email);

        List<SeatHold> allHoldsOfUser = getSeatHoldByUserId(user.getId());

        if (allHoldsOfUser.isEmpty())
            return;

        List<Seat> seats = allHoldsOfUser.stream()
                .map(SeatHold::getSeat)
                .toList();

        for (Seat seat : seats) {
            seat.setStatus(SeatStatusEnum.AVAILABLE);
        }

        seatRepository.saveAll(seats);
        seatHoldRepository.deleteAll(allHoldsOfUser);
    }

    public List<SeatHold> getSeatHoldByUserId(Long id) {
        List<SeatHold> listItem = this.seatHoldRepository.findByUserIdFetchFull(id);
        return listItem;
    }
}
