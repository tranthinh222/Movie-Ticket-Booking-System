package com.cinema.ticketbooking.controller;

import com.cinema.ticketbooking.domain.SeatVariant;
import com.cinema.ticketbooking.domain.request.ReqCreateSeatVariantDto;
import com.cinema.ticketbooking.domain.request.ReqUpdateSeatVariantDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.service.SeatVariantService;
import com.cinema.ticketbooking.util.annotation.ApiMessage;
import com.cinema.ticketbooking.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1")
public class SeatVariantController {
    private final SeatVariantService seatVariantService;

    public SeatVariantController(SeatVariantService seatVariantService) {
        this.seatVariantService = seatVariantService;
    }

    @GetMapping("/seat-variants")
    @ApiMessage("fetch all seatvariants")
    public ResponseEntity<ResultPaginationDto> getAllSeatVariants(
            @Filter Specification<SeatVariant> spec, Pageable pageable) {
        return ResponseEntity.status(HttpStatus.OK).body(this.seatVariantService.getAllSeatVariants(spec, pageable));
    }

    @GetMapping("/seat-variants/{id}")
    @ApiMessage("fetch a seat variant")
    public ResponseEntity<SeatVariant> getSeatVariantById(@PathVariable Long id) {
        SeatVariant seat = this.seatVariantService.findSeatVariantById(id);
        if (seat == null) {
            throw new IdInvalidException("Seat variant with id " + id + " not found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(seat);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/seat-variants")
    @ApiMessage("create a seat variant")
    public ResponseEntity<SeatVariant> createSeatVariant(@Valid @RequestBody ReqCreateSeatVariantDto reqSeat) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.seatVariantService.createSeatVariant(reqSeat));

    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/seat-variants/{id}")
    @ApiMessage("delete a seat variant")
    public ResponseEntity<Void> deleteSeatVariant(@PathVariable Long id) {
        SeatVariant seatVariant = this.seatVariantService.findSeatVariantById(id);
        if (seatVariant == null) {
            throw new IdInvalidException("seat with id " + id + " not found");
        }
        this.seatVariantService.deleteSeatVariant(id);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/seat-variants")
    public ResponseEntity<SeatVariant> updateSeat(@Valid @RequestBody ReqUpdateSeatVariantDto reqSeat) {
        SeatVariant newSeat = this.seatVariantService.updateSeatVariant(reqSeat);
        if (newSeat == null) {
            throw new IdInvalidException("Seat variant with id " + newSeat.getId() + " does not exist");
        }

        return ResponseEntity.status(HttpStatus.OK).body(newSeat);
    }
}
