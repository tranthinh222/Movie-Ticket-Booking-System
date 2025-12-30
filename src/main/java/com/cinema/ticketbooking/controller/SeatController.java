package com.cinema.ticketbooking.controller;

import com.cinema.ticketbooking.domain.Auditorium;
import com.cinema.ticketbooking.domain.Seat;
import com.cinema.ticketbooking.domain.SeatVariant;
import com.cinema.ticketbooking.domain.request.ReqCreateSeatDto;
import com.cinema.ticketbooking.domain.request.ReqUpdateSeatDto;
import com.cinema.ticketbooking.domain.response.ResAuditoriumDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.service.AuditoriumService;
import com.cinema.ticketbooking.service.SeatService;
import com.cinema.ticketbooking.service.SeatVariantService;
import com.cinema.ticketbooking.util.annotation.ApiMessage;
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
public class SeatController {
    private final SeatService seatService;
    private final AuditoriumService auditoriumService;
    private final SeatVariantService seatVariantService;

    public SeatController(SeatService seatService, AuditoriumService auditoriumService,
            SeatVariantService seatVariantService) {
        this.seatService = seatService;
        this.auditoriumService = auditoriumService;
        this.seatVariantService = seatVariantService;
    }

    @GetMapping("/seats")
    @ApiMessage("fetch all seats")
    public ResponseEntity<ResultPaginationDto> getAllSeats(
            @Filter Specification<Seat> spec, Pageable pageable) {

        return ResponseEntity.status(HttpStatus.OK).body(this.seatService.getAllSeats(spec, pageable));
    }

    @GetMapping("/seats/{id}")
    @ApiMessage("fetch a seat")
    public ResponseEntity<Seat> getSeatById(@PathVariable Long id) {
        Seat seat = this.seatService.findSeatById(id);
        if (seat == null) {
            throw new IdInvalidException("Seat with id " + id + " not found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(seat);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/seats/{id}")
    @ApiMessage("delete a seat")
    public ResponseEntity<Void> deleteSeat(@PathVariable Long id) {
        Seat seat = this.seatService.findSeatById(id);
        if (seat == null) {
            throw new IdInvalidException("seat with id " + id + " not found");
        }
        this.seatService.deleteSeat(id);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/seats")
    @ApiMessage("create a seat")
    public ResponseEntity<Seat> createSeat(@Valid @RequestBody ReqCreateSeatDto reqSeat) {
        Auditorium auditorium = this.auditoriumService.getAuditoriumById(reqSeat.getAuditoriumId());

        if (auditorium == null) {
            throw new IdInvalidException("Auditorium with id " + reqSeat.getAuditoriumId() + " not found");
        }
        SeatVariant seatVariant = this.seatVariantService.findSeatVariantById(reqSeat.getSeatVariantId());

        if (seatVariant == null) {
            throw new IdInvalidException("SeatVariant with id " + reqSeat.getSeatVariantId() + " not found");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(this.seatService.createSeat(reqSeat));

    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/seats")
    public ResponseEntity<Seat> updateSeat(@Valid @RequestBody ReqUpdateSeatDto reqSeat) {
        Seat newSeat = this.seatService.updateSeat(reqSeat);
        if (newSeat == null) {
            throw new IdInvalidException("Seat with id " + newSeat.getId() + " does not exist");
        }

        return ResponseEntity.status(HttpStatus.OK).body(newSeat);
    }

}
