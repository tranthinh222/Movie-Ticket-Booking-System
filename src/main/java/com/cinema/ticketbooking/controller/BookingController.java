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
import com.cinema.ticketbooking.domain.BookingItem;
import com.cinema.ticketbooking.domain.Film;
import com.cinema.ticketbooking.domain.ShowTime;
import com.cinema.ticketbooking.domain.Theater;
import com.cinema.ticketbooking.domain.User;
import com.cinema.ticketbooking.domain.request.ReqCreateBookingDto;
import com.cinema.ticketbooking.domain.request.ReqUpdateBookingStatusDto;
import com.cinema.ticketbooking.domain.response.ResBookingDto;
import com.cinema.ticketbooking.domain.response.ResCreateBookingDto;
import com.cinema.ticketbooking.domain.response.ResRevenueDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.service.BookingService;
import com.cinema.ticketbooking.service.UserService;
import com.cinema.ticketbooking.util.SecurityUtil;
import com.cinema.ticketbooking.util.annotation.ApiMessage;
import com.cinema.ticketbooking.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;

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
    public ResponseEntity<ResCreateBookingDto> createBooking(
            @Valid @RequestBody ReqCreateBookingDto request,
            HttpServletRequest httpRequest) {
        Long userId = SecurityUtil.getCurrentUserId()
                .orElseThrow(() -> new IdInvalidException("User not authenticated"));

        User user = this.userService.getUserById(userId);
        if (user == null) {
            throw new IdInvalidException("User not found");
        }

        // Get client IP address
        String ipAddress = getClientIp(httpRequest);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(this.bookingService.createBooking(user.getId(), request.getPaymentMethod(), ipAddress));
    }

    private String getClientIp(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        // If multiple IPs, take the first one
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0];
        }
        return ipAddress;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/bookings")
    @ApiMessage("fetch all bookings")
    public ResponseEntity<ResultPaginationDto> getAllBookings(
            @Filter Specification<Booking> spec,
            Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(this.bookingService.getAllBookings(spec, pageable));
    }

    @GetMapping("/bookings/{id}")
    @ApiMessage("fetch booking by id")
    public ResponseEntity<ResBookingDto> getBookingById(@PathVariable("id") Long id) {
        Booking booking = this.bookingService.getBookingById(id);
        if (booking == null) {
            throw new IdInvalidException("Booking not found");
        }

        ResBookingDto response = new ResBookingDto();
        response.setId(booking.getId());
        response.setUser(new ResBookingDto.UserInfo(booking.getUser().getId(), booking.getUser().getUsername()));
        response.setStatus(booking.getStatus());
        response.setTotal_price(booking.getTotal_price());
        response.setQrCode(booking.getQrCode());

        // Map seats from booking items
        if (booking.getBookingItems() != null && !booking.getBookingItems().isEmpty()) {
            java.util.List<ResBookingDto.SeatInfo> seats = booking.getBookingItems().stream()
                    .map(item -> new ResBookingDto.SeatInfo(
                            item.getSeat().getId(),
                            item.getSeat().getSeatRow(),
                            item.getSeat().getNumber(),
                            item.getPrice()))
                    .collect(java.util.stream.Collectors.toList());
            response.setSeats(seats);

            // Get showtime from first booking item
            BookingItem firstItem = booking.getBookingItems().get(0);
            if (firstItem.getShowTime() != null) {
                ShowTime showtime = firstItem.getShowTime();

                // Map showtime info
                ResBookingDto.ShowTimeInfo showtimeInfo = new ResBookingDto.ShowTimeInfo(
                        showtime.getId(),
                        showtime.getDate(),
                        showtime.getStartTime(),
                        showtime.getEndTime(),
                        showtime.getAuditorium() != null ? String.valueOf(showtime.getAuditorium().getNumber()) : null);
                response.setShowtime(showtimeInfo);

                // Map film info
                if (showtime.getFilm() != null) {
                    Film film = showtime.getFilm();
                    ResBookingDto.FilmInfo filmInfo = new ResBookingDto.FilmInfo(
                            film.getId(),
                            film.getName(),
                            film.getDirector(),
                            film.getActors(),
                            film.getDuration(),
                            film.getDescription(),
                            film.getGenre(),
                            film.getLanguage(),
                            film.getReleaseDate(),
                            film.getStatus(),
                            film.getThumbnail());
                    response.setFilm(filmInfo);
                }

                // Map theater info
                if (showtime.getAuditorium() != null && showtime.getAuditorium().getTheater() != null) {
                    Theater theater = showtime.getAuditorium().getTheater();
                    String addressStr = null;
                    if (theater.getAddress() != null) {
                        addressStr = String.format("%s %s, %s",
                                theater.getAddress().getStreet_number() != null
                                        ? theater.getAddress().getStreet_number()
                                        : "",
                                theater.getAddress().getStreet_name() != null ? theater.getAddress().getStreet_name()
                                        : "",
                                theater.getAddress().getCity() != null ? theater.getAddress().getCity() : "").trim();
                    }
                    ResBookingDto.TheaterInfo theaterInfo = new ResBookingDto.TheaterInfo(
                            theater.getId(),
                            theater.getName(),
                            addressStr);
                    response.setTheater(theaterInfo);
                }
            }
        }

        response.setCreatedAt(booking.getCreatedAt());
        response.setUpdatedAt(booking.getUpdatedAt());
        response.setCreatedBy(booking.getCreatedBy());
        response.setUpdatedBy(booking.getUpdatedBy());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{userId}/bookings")
    @ApiMessage("fetch all bookings of a user")
    public ResponseEntity<ResultPaginationDto> getBookingsByUserId(
            @PathVariable("userId") Long userId,
            Pageable pageable) {
        return ResponseEntity.ok(this.bookingService.getBookingsByUserId(userId, pageable));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/bookings/{id}/status")
    @ApiMessage("update booking status")
    public ResponseEntity<ResBookingDto> updateBookingStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody ReqUpdateBookingStatusDto request) {
        Booking booking = this.bookingService.getBookingById(id);
        if (booking == null) {
            throw new IdInvalidException("Booking not found");
        }

        Booking updatedBooking = this.bookingService.updateBookingStatus(id, request.getStatus());

        ResBookingDto response = new ResBookingDto();
        response.setId(updatedBooking.getId());
        response.setUser(
                new ResBookingDto.UserInfo(updatedBooking.getUser().getId(), updatedBooking.getUser().getUsername()));
        response.setStatus(updatedBooking.getStatus());
        response.setTotal_price(updatedBooking.getTotal_price());
        response.setQrCode(updatedBooking.getQrCode());
        response.setCreatedAt(updatedBooking.getCreatedAt());
        response.setUpdatedAt(updatedBooking.getUpdatedAt());
        response.setCreatedBy(updatedBooking.getCreatedBy());
        response.setUpdatedBy(updatedBooking.getUpdatedBy());

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/bookings/revenue/current-month")
    @ApiMessage("get revenue for current month")
    public ResponseEntity<ResRevenueDto> getCurrentMonthRevenue() {
        return ResponseEntity.ok(this.bookingService.getCurrentMonthRevenue());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/bookings/revenue")
    @ApiMessage("get revenue by month and year")
    public ResponseEntity<ResRevenueDto> getRevenueByMonth(
            @RequestParam("month") int month,
            @RequestParam("year") int year) {
        if (month < 1 || month > 12) {
            throw new IdInvalidException("Month must be between 1 and 12");
        }
        if (year < 2000 || year > 2100) {
            throw new IdInvalidException("Year must be between 2000 and 2100");
        }
        return ResponseEntity.ok(this.bookingService.getRevenueByMonth(month, year));
    }

}
