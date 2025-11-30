package com.cinema.ticketbooking.controller;

import com.cinema.ticketbooking.domain.Film;
import com.cinema.ticketbooking.domain.request.ReqCreateFilmDto;
import com.cinema.ticketbooking.domain.request.ReqUpdateFilmDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.service.FilmService;
import com.cinema.ticketbooking.util.annotation.ApiMessage;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1")
public class FilmController {
    private final FilmService filmService;
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping ("/films")
    @ApiMessage("fetch all films")
    public ResponseEntity<ResultPaginationDto> getAllFilms(
            @Filter Specification<Film> spec, Pageable pageable
    ){

        return ResponseEntity.status(HttpStatus.OK).body(this.filmService.getAllFilms(spec, pageable));
    }

    @PostMapping("/films")
    @ApiMessage("create a film")
    public ResponseEntity<Film> createFilm (@Valid @RequestBody ReqCreateFilmDto reqFilm) throws Exception {
        boolean isFilmExisted = this.filmService.isFilmNameDuplicated(reqFilm.getName());
        if (isFilmExisted){
            throw new Exception("Film with name " + reqFilm.getName() + " already exists");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(this.filmService.createFilm(reqFilm));

    }

    @DeleteMapping("/films/{id}")
    @ApiMessage("delete a film")
    public ResponseEntity<Void> deleteFilm (@PathVariable Long id) throws Exception{
        Film film = this.filmService.getFilmById(id);
        if (film == null)
        {
            throw new Exception("Film with id "+ id +" not found");
        }

        this.filmService.deleteFilm(film);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @PutMapping("/films")
    public ResponseEntity<Film> updateFilm (@Valid @RequestBody ReqUpdateFilmDto reqFilm) throws Exception{
        Film newFilm = this.filmService.updateFilm(reqFilm);
        if (newFilm == null){
            throw new Exception("Film with id " + newFilm.getId() + " does not exist");
        }

        return ResponseEntity.status(HttpStatus.OK).body(newFilm);
    }
}
