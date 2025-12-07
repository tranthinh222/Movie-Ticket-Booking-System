package com.cinema.ticketbooking.controller;

import com.cinema.ticketbooking.domain.Auditorium;
import com.cinema.ticketbooking.domain.Seat;
import com.cinema.ticketbooking.domain.SeatVariant;
import com.cinema.ticketbooking.domain.Theater;
import com.cinema.ticketbooking.domain.request.ReqCreateSeatDto;
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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1")
public class SeatVariantController {
    private final SeatVariantService seatVariantService;
    public SeatVariantController(SeatVariantService seatVariantService) {
        this.seatVariantService = seatVariantService;
    }

    @GetMapping("/seatvariants")
    @ApiMessage("fetch all seatvariants")
    public ResponseEntity<ResultPaginationDto> getAllSeatVariants(
            @Filter Specification<SeatVariant> spec, Pageable pageable)
    {
        return ResponseEntity.status(HttpStatus.OK).body(this.seatVariantService.getAllSeatVariants(spec, pageable));
    }

    @GetMapping ("/seatvariants/{id}")
    @ApiMessage("fetch a seat variant")
    public ResponseEntity<SeatVariant> getSeatVariantById(@PathVariable Long id){
        SeatVariant seat = this.seatVariantService.findSeatVariantById(id);
        if (seat == null)
        {
            throw new IdInvalidException("Seat variant with id " + id + " not found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(seat);
    }

//    @PostMapping("/seatvariants")
//    @ApiMessage("create a seat variant")
//    public ResponseEntity<Seat> createSeat (@Valid @RequestBody ReqCreateSeatDto reqSeat) {
//        Auditorium auditorium = this.auditoriumService.getAuditoriumById(reqSeat.getAuditoriumId());
//
//        if (auditorium == null)
//        {
//            throw new IdInvalidException("Auditorium with id " + reqSeat.getAuditoriumId() + " not found");
//        }
//        return ResponseEntity.status(HttpStatus.CREATED).body(this.seatService.createSeat(reqSeat));
//
//    }

}
