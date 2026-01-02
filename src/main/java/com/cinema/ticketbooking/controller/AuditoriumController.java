package com.cinema.ticketbooking.controller;

import com.cinema.ticketbooking.domain.Auditorium;
import com.cinema.ticketbooking.domain.Theater;
import com.cinema.ticketbooking.domain.request.ReqCreateAuditoriumDto;
import com.cinema.ticketbooking.domain.request.ReqUpdateAuditoriumDto;
import com.cinema.ticketbooking.domain.response.ResAuditoriumDto;
import com.cinema.ticketbooking.domain.response.ResSeatDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.service.AuditoriumService;
import com.cinema.ticketbooking.service.TheaterService;
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
public class AuditoriumController {
    private final AuditoriumService auditoriumService;
    private final TheaterService theaterService;

    public AuditoriumController(AuditoriumService auditoriumService, TheaterService theaterService) {
        this.auditoriumService = auditoriumService;
        this.theaterService = theaterService;
    }

    @GetMapping("/auditoriums")
    @ApiMessage("fetch all auditoriums")
    public ResponseEntity<ResultPaginationDto> getAlls(
            @Filter Specification<Auditorium> spec, Pageable pageable) {

        return ResponseEntity.status(HttpStatus.OK).body(this.auditoriumService.getAllAuditoriums(spec, pageable));
    }

    @GetMapping("/auditoriums/{id}")
    @ApiMessage("fetch an auditorium")
    public ResponseEntity<Auditorium> getAuditorium(@PathVariable("id") Long id) {
        Auditorium auditorium = this.auditoriumService.getAuditoriumById(id);
        if (auditorium == null) {
            throw new IdInvalidException("Auditorium with id " + id + " not found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(auditorium);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/auditoriums")
    @ApiMessage("create a auditorium")
    public ResponseEntity<Auditorium> createAuditorium(@Valid @RequestBody ReqCreateAuditoriumDto reqAuditorium)
            throws Exception {
        Theater theater = this.theaterService.findTheaterById(reqAuditorium.getTheaterId());
        if (theater == null) {
            throw new IdInvalidException("theater with id " + reqAuditorium.getTheaterId() + " not found");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(this.auditoriumService.createAuditorium(reqAuditorium));

    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/auditoriums/{id}")
    @ApiMessage("delete a auditorium")
    public ResponseEntity<Void> deleteAuditorium(@PathVariable("id") Long id) throws Exception {
        Auditorium auditorium = this.auditoriumService.getAuditoriumById(id);
        if (auditorium == null) {
            throw new Exception("auditorium with id " + id + " not found");
        }

        this.auditoriumService.deleteAuditorium(id);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/auditoriums")
    public ResponseEntity<Auditorium> updateAuditorium(@Valid @RequestBody ReqUpdateAuditoriumDto reqAuditorium)
            throws Exception {
        Auditorium newAuditorium = this.auditoriumService.updateAuditorium(reqAuditorium);
        if (newAuditorium == null) {
            throw new Exception("auditorium with id " + reqAuditorium.getId() + " does not exist");
        }

        return ResponseEntity.status(HttpStatus.OK).body(newAuditorium);
    }

    @GetMapping("/seats/auditorium/{id}")
    @ApiMessage("fetch seat by auditorium")
    public ResponseEntity<List<ResSeatDto>> getSeatsByAuditoriumId(@PathVariable("id") Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(this.auditoriumService.getSeatByAuditoriumId(id));
    }

}
