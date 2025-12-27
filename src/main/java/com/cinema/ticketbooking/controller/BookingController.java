package com.cinema.ticketbooking.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cinema.ticketbooking.domain.Booking;
import com.cinema.ticketbooking.domain.User;
import com.cinema.ticketbooking.domain.request.ReqCreateBookingDto;
import com.cinema.ticketbooking.service.BookingService;
import com.cinema.ticketbooking.service.UserService;
import com.cinema.ticketbooking.util.error.IdInvalidException;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;

@RestController
@RequestMapping("/api/v1")
public class BookingController {
    private final BookingService bookingService;
    private final UserService userService;

    BookingController(BookingService bookingService, UserService userService) {
        this.bookingService = bookingService;
        this.userService = userService;
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @PostMapping("/bookings")
    public ResponseEntity<Booking> createBooking(@Valid @RequestBody ReqCreateBookingDto reqBooking) {
        User user = this.userService.getUserById(reqBooking.getUserId());
        if (user == null) {
            throw new IdInvalidException("user id khong ton tai");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(this.bookingService.createBooking(reqBooking));
    }

}
