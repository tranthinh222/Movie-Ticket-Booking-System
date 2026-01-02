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
import com.cinema.ticketbooking.repository.BookingItemRepository;
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
    private final BookingItemRepository bookingItemRepository;

    public SeatHoldService(SeatHoldRepository seatHoldRepository, UserRepository userRepository,
            ShowTimeRepository showTimeRepository, SeatRepository seatRepository,
            SeatService seatService, BookingItemRepository bookingItemRepository) {
        this.seatHoldRepository = seatHoldRepository;
        this.userRepository = userRepository;
        this.showTimeRepository = showTimeRepository;
        this.seatRepository = seatRepository;
        this.bookingItemRepository = bookingItemRepository;
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

        // Kiểm tra xem ghế đã được hold hoặc booked cho showtime này chưa
        for (Seat seat : seats) {
            // Kiểm tra SeatHold hiện tại cho showtime này
            boolean isHeld = seatHoldRepository.existsBySeatIdAndShowTimeId(seat.getId(), showtime.getId());
            if (isHeld) {
                throw new RuntimeException("Seat " + seat.getId() + " is already held for this showtime");
            }

            // Kiểm tra BookingItem (ghế đã được booking) cho showtime này
            boolean isBooked = bookingItemRepository.existsBySeatIdAndShowTimeId(seat.getId(), showtime.getId());
            if (isBooked) {
                throw new RuntimeException("Seat " + seat.getId() + " is already booked for this showtime");
            }
        }

        Instant expiresAt = Instant.now().plus(5, ChronoUnit.MINUTES);

        List<SeatHold> seatHolds = new ArrayList<>();

        for (Seat seat : seats) {
            SeatHold hold = new SeatHold();
            hold.setSeat(seat);
            hold.setShowTime(showtime);
            hold.setUser(user);
            hold.setExpiresAt(expiresAt);

            seatHolds.add(hold);
        }

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

        // Chỉ xóa SeatHold, không cần cập nhật Seat status
        seatHoldRepository.deleteAll(allHoldsOfUser);
    }

    public List<SeatHold> getSeatHoldByUserId(Long id) {
        List<SeatHold> listItem = this.seatHoldRepository.findByUserIdFetchFull(id);
        return listItem;
    }
}
