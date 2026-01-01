package com.cinema.ticketbooking.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cinema.ticketbooking.domain.Booking;
import com.cinema.ticketbooking.domain.User;
import com.cinema.ticketbooking.domain.request.ReqCreateBookingDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.service.BookingService;
import com.cinema.ticketbooking.service.UserService;
import com.cinema.ticketbooking.util.SecurityUtil;
import com.cinema.ticketbooking.util.annotation.ApiMessage;
import com.cinema.ticketbooking.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/v1")
public class BookingController {
    private final BookingService bookingService;
    private final UserService userService;

    BookingController(BookingService bookingService, UserService userService) {
        this.bookingService = bookingService;
        this.userService = userService;
    }

    @PostMapping("/bookings")
    public ResponseEntity<Booking> createBooking() {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new IdInvalidException("User not authenticated"));

        User user = this.userService.getUserByEmail(email);
        if (user == null) {
            throw new IdInvalidException("User not found");
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(this.bookingService.createBooking(user.getId()));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/bookings")
    @ApiMessage("fetch all bookings")
    public ResponseEntity<ResultPaginationDto> getAllBookings(
            @Filter Specification<Booking> spec,
            Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(this.bookingService.getAllBookings(spec, pageable));
    }

}
