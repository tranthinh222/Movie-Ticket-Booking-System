package com.cinema.ticketbooking.controller;

import com.cinema.ticketbooking.domain.Address;
import com.cinema.ticketbooking.domain.Auditorium;
import com.cinema.ticketbooking.domain.Theater;
import com.cinema.ticketbooking.domain.request.ReqCreateTheaterDto;
import com.cinema.ticketbooking.domain.request.ReqUpdateTheaterDto;
import com.cinema.ticketbooking.domain.response.ResAuditoriumDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.service.AddressService;
import com.cinema.ticketbooking.service.TheaterService;
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

import java.util.List;


@RestController
@RequestMapping("api/v1")
public class TheaterController {
    private final TheaterService theaterService;
    private final AddressService addressService;
    public TheaterController(TheaterService theaterService,  AddressService addressService) {
        this.theaterService = theaterService;
        this.addressService = addressService;
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @GetMapping("/theaters")
    @ApiMessage("fetch all theateres")
    public ResponseEntity<ResultPaginationDto> getAllTheaters(
            @Filter Specification<Theater> spec, Pageable pageable)
    {
        return ResponseEntity.status(HttpStatus.OK).body(this.theaterService.getAllTheaters(spec, pageable));
    }



    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/theaters/{id}")
    @ApiMessage("fetch a theater")
    public ResponseEntity<Theater> getTheater(@PathVariable Long id)
    {
        Theater theater = this.theaterService.findTheaterById(id);
        if  (theater == null) {
            throw new IdInvalidException("theater with id " + id + " does not exist");
        }
        return ResponseEntity.status(HttpStatus.OK).body(theater);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/theaters")
    @ApiMessage("create a theater")
    public ResponseEntity<Theater> createtheater(@Valid @RequestBody ReqCreateTheaterDto theater){
        Address address = this.addressService.findAddressById(theater.getAddressId());
        if (address == null){
            throw new IdInvalidException("Address with id "+ theater.getAddressId() +" not found");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(this.theaterService.createTheater(theater));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/theaters/{id}")
    @ApiMessage("delete a theater")
    public ResponseEntity<Void> deleteTheater (@PathVariable Long id) {
        Theater theater = this.theaterService.findTheaterById(id);
        if (theater == null)
        {
            throw new IdInvalidException("theater with id "+ id +" not found");
        }
        this.theaterService.deleteTheater(id);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/theaters")
    public ResponseEntity<Theater> updateTheater (@Valid @RequestBody ReqUpdateTheaterDto reqTheater) {
        Theater newTheater = this.theaterService.updateTheater(reqTheater);
        if (newTheater == null){
            throw new IdInvalidException("Theater with id " + reqTheater.getId() + " does not exist");
        }

        return ResponseEntity.status(HttpStatus.OK).body(newTheater);
    }

    @GetMapping("/auditoriums/theater/{id}")
    @ApiMessage("fetch auditoriums by theater")
    public ResponseEntity<List<ResAuditoriumDto>> getAuditoriumsByTheaterId(@PathVariable("id") Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(this.theaterService.getAuditoriumsByTheaterId(id));
    }
}
