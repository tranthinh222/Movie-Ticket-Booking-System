package com.cinema.ticketbooking.controller;

import com.cinema.ticketbooking.domain.Film;
import com.cinema.ticketbooking.domain.request.ReqCreateFilmDto;
import com.cinema.ticketbooking.domain.request.ReqUpdateFilmDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.service.FilmService;
import com.cinema.ticketbooking.util.annotation.ApiMessage;
import com.cinema.ticketbooking.util.error.IdInvalidException;
import com.cinema.ticketbooking.util.error.ResourceAlreadyExistsException;
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
public class FilmController {
    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping("/films")
    @ApiMessage("fetch all films")
    public ResponseEntity<ResultPaginationDto> getAllFilms(
            @Filter Specification<Film> spec, Pageable pageable) {

        return ResponseEntity.status(HttpStatus.OK).body(this.filmService.getAllFilms(spec, pageable));
    }

    @GetMapping("/films/{id}")
    @ApiMessage("fetch a film")
    public ResponseEntity<Film> getFilm(@PathVariable Long id) {
        Film film = this.filmService.getFilmById(id);
        if (film == null) {
            throw new IdInvalidException("film with id " + id + " not found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(film);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/films")
    @ApiMessage("create a film")
    public ResponseEntity<Film> createFilm(@Valid @RequestBody ReqCreateFilmDto reqFilm) {
        boolean isFilmExisted = this.filmService.isFilmNameDuplicated(reqFilm.getName());
        if (isFilmExisted) {
            throw new ResourceAlreadyExistsException(
                    "Film with name " + reqFilm.getName() + " already exists");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(this.filmService.createFilm(reqFilm));

    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/films/{id}")
    @ApiMessage("delete a film")
    public ResponseEntity<Void> deleteFilm(@PathVariable("id") Long id) throws Exception {
        Film film = this.filmService.getFilmById(id);
        if (film == null) {
            throw new IdInvalidException("Film with id " + id + " not found");
        }

        this.filmService.deleteFilm(id);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/films")
    @ApiMessage("update a film")
    public ResponseEntity<Film> updateFilm(@Valid @RequestBody ReqUpdateFilmDto reqFilm) {
        Film newFilm = this.filmService.updateFilm(reqFilm);
        if (newFilm == null) {
            throw new IdInvalidException("Film with id " + reqFilm.getId() + " does not exist");
        }

        return ResponseEntity.status(HttpStatus.OK).body(newFilm);
    }
}
