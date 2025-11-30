package com.cinema.ticketbooking.controller;

import com.cinema.ticketbooking.domain.Theater;
import com.cinema.ticketbooking.domain.request.ReqCreateTheaterDto;
import com.cinema.ticketbooking.domain.request.ReqUpdateTheaterDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.service.TheaterService;
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
public class TheaterController {
    private final TheaterService theaterService;
    public TheaterController(TheaterService theaterService) {
        this.theaterService = theaterService;
    }

    @GetMapping("/theaters")
    public ResponseEntity<ResultPaginationDto> getAllTheaters(
            @Filter Specification<Theater> spec, Pageable pageable)
    {
        return ResponseEntity.status(HttpStatus.OK).body(this.theaterService.getAllTheaters(spec, pageable));
    }

    @PostMapping("/theaters")
    @ApiMessage("create a theater")
    public ResponseEntity<Theater> createtheater(@RequestBody ReqCreateTheaterDto theater){
        return ResponseEntity.status(HttpStatus.CREATED).body(this.theaterService.createTheater(theater));
    }

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

//    @PutMapping("/theaters")
//    public ResponseEntity<Theater> updateTheater (@Valid @RequestBody ReqUpdateTheaterDto reqTheater) {
//        Theater newTheater = this.theaterService.updateTheater(reqTheater);
//        if (newTheater == null){
//            throw new IdInvalidException("Theater with id " + reqTheater.getId() + " does not exist");
//        }
//
//        return ResponseEntity.status(HttpStatus.OK).body(newTheater);
//    }


}
