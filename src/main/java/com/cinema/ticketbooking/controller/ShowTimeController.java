package com.cinema.ticketbooking.controller;

import com.cinema.ticketbooking.domain.Auditorium;
import com.cinema.ticketbooking.domain.Film;
import com.cinema.ticketbooking.domain.ShowTime;
import com.cinema.ticketbooking.domain.request.ReqCreateShowTimeDto;
import com.cinema.ticketbooking.domain.request.ReqUpdateShowTimeDto;
import com.cinema.ticketbooking.domain.response.ResSeatAvailabilityDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.service.AuditoriumService;
import com.cinema.ticketbooking.service.FilmService;
import com.cinema.ticketbooking.service.SeatService;
import com.cinema.ticketbooking.service.ShowTimeService;
import com.cinema.ticketbooking.util.annotation.ApiMessage;
import com.cinema.ticketbooking.util.error.BadRequestException;
import com.cinema.ticketbooking.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1")
public class ShowTimeController {
    private final ShowTimeService showTimeService;
    private final FilmService filmService;
    private final AuditoriumService auditoriumService;
    private final SeatService seatService;

    public ShowTimeController(ShowTimeService showTimeService, FilmService filmService,
            AuditoriumService auditoriumService, SeatService seatService) {
        this.showTimeService = showTimeService;
        this.filmService = filmService;
        this.auditoriumService = auditoriumService;
        this.seatService = seatService;
    }

    @GetMapping("/showtimes")
    @ApiMessage("fetch all showtimes")
    public ResponseEntity<ResultPaginationDto> getAllShowTimes(
            @Filter Specification<ShowTime> spec, Pageable pageable) {

        return ResponseEntity.status(HttpStatus.OK).body(this.showTimeService.getAllShowTimes(spec, pageable));
    }

    @GetMapping("/showtimes/{id}")
    @ApiMessage("fetch an showtime")
    public ResponseEntity<ShowTime> getShowTimeById(@PathVariable Long id) {
        ShowTime showTime = this.showTimeService.findShowTimeById(id);
        if (showTime == null) {
            throw new IdInvalidException("ShowTime with id " + id + " not found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(showTime);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/showtimes")
    @ApiMessage("create an showtime")
    public ResponseEntity<ShowTime> create(@Valid @RequestBody ReqCreateShowTimeDto reqShowTime) {
        Film film = filmService.getFilmById(reqShowTime.getFilmId());
        if (film == null) {
            throw new IdInvalidException("Film with id " + reqShowTime.getFilmId() + " not found");
        }

        Auditorium auditorium = this.auditoriumService.getAuditoriumById(reqShowTime.getAuditoriumId());
        if (auditorium == null) {
            throw new IdInvalidException("Auditorium with id " + reqShowTime.getAuditoriumId() + " not found");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(this.showTimeService.createShowTime(reqShowTime));

    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/showtimes/{id}")
    @ApiMessage("delete an showtime")
    public ResponseEntity<Void> deleteShowTime(@PathVariable Long id) {
        ShowTime showTime = this.showTimeService.findShowTimeById(id);
        if (showTime == null) {
            throw new IdInvalidException("ShowTime with id " + id + " not found");
        }

        // Check if showtime has any bookings
        if (this.showTimeService.hasBookings(id)) {
            throw new BadRequestException("Cannot delete showtime with id " + id + " because it has existing bookings");
        }

        this.showTimeService.deleteShowTime(id);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/showtimes")
    public ResponseEntity<ShowTime> updateShowTime(@Valid @RequestBody ReqUpdateShowTimeDto reqShowTime) {
        ShowTime existingShowTime = this.showTimeService.findShowTimeById(reqShowTime.getId());
        if (existingShowTime == null) {
            throw new IdInvalidException("ShowTime with id " + reqShowTime.getId() + " does not exist");
        }

        // Check if showtime has any bookings
        if (this.showTimeService.hasBookings(reqShowTime.getId())) {
            throw new BadRequestException(
                    "Cannot update showtime with id " + reqShowTime.getId() + " because it has existing bookings");
        }

        ShowTime newShowTime = this.showTimeService.updateShowTime(reqShowTime);
        return ResponseEntity.status(HttpStatus.OK).body(newShowTime);
    }

    /**
     * API mới: Lấy trạng thái ghế theo showtime
     */
    @GetMapping("/showtimes/{id}/seats")
    @ApiMessage("fetch seat availability for showtime")
    public ResponseEntity<List<ResSeatAvailabilityDto>> getSeatAvailability(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(this.seatService.getSeatAvailabilityByShowTime(id));
    }
}
