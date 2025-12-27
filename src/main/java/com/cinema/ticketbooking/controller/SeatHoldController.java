package com.cinema.ticketbooking.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cinema.ticketbooking.domain.Seat;
import com.cinema.ticketbooking.domain.SeatHold;
import com.cinema.ticketbooking.domain.User;
import com.cinema.ticketbooking.domain.request.ReqCreateSeatHoldDto;
import com.cinema.ticketbooking.service.SeatHoldService;
import com.cinema.ticketbooking.service.SeatService;
import com.cinema.ticketbooking.service.UserService;
import com.cinema.ticketbooking.util.constant.SeatStatusEnum;
import com.cinema.ticketbooking.util.error.IdInvalidException;
import com.cinema.ticketbooking.util.error.UnavailableResourceException;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("api/v1")
public class SeatHoldController {
    private final SeatHoldService seatHoldService;
    private final SeatService seatService;
    private final UserService userService;

    public SeatHoldController(SeatHoldService seatHoldService, SeatService seatService, UserService userService) {
        this.seatHoldService = seatHoldService;
        this.seatService = seatService;
        this.userService = userService;
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/seat-holds")
    public ResponseEntity<SeatHold> createSeatHold(@Valid @RequestBody ReqCreateSeatHoldDto reqSeatHold) {
        Seat seat = this.seatService.findSeatById(reqSeatHold.getSeatId());
        if (seat == null) {
            throw new IdInvalidException("Seat with id " + reqSeatHold.getSeatId() + " does not exist");
        }
        boolean isEmpty = seat.getStatus() == SeatStatusEnum.AVAILABLE;
        if (!isEmpty) {
            throw new UnavailableResourceException("Seat is booked or hold");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(this.seatHoldService.createSeatHold(reqSeatHold));
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @GetMapping("/seat-holds/{id}")
    public ResponseEntity<List<SeatHold>> getMethodName(@PathVariable Long id) {
        User user = this.userService.getUserById(id);
        if (user == null) {
            throw new IdInvalidException("id user không hợp lệ");
        }
        return ResponseEntity.status(HttpStatus.OK).body(this.seatHoldService.getSeatHoldByUserId(id));
    }
}
