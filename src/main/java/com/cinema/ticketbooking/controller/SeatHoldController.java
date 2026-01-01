package com.cinema.ticketbooking.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cinema.ticketbooking.domain.SeatHold;
import com.cinema.ticketbooking.domain.User;
import com.cinema.ticketbooking.domain.request.ReqCreateSeatHoldDto;
import com.cinema.ticketbooking.service.SeatHoldService;
import com.cinema.ticketbooking.service.SeatService;
import com.cinema.ticketbooking.service.UserService;
import com.cinema.ticketbooking.util.error.IdInvalidException;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    @PostMapping("/seat-holds")
    public ResponseEntity<Void> createSeatHold(@Valid @RequestBody ReqCreateSeatHoldDto reqSeatHold) {
        this.seatHoldService.createSeatHold(reqSeatHold);
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    @DeleteMapping("/seat-holds")
    public ResponseEntity<Void> removeSeatHold() {
        this.seatHoldService.removeSeatHold();
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(null);
    }

    @GetMapping("/seat-holds/{id}")
    public ResponseEntity<List<SeatHold>> getMethodName(@PathVariable Long id) {
        User user = this.userService.getUserById(id);
        if (user == null) {
            throw new IdInvalidException("id user không hợp lệ");
        }
        return ResponseEntity.status(HttpStatus.OK).body(this.seatHoldService.getSeatHoldByUserId(id));
    }
}
