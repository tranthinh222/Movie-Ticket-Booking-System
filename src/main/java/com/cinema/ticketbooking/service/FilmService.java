package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.Film;
import com.cinema.ticketbooking.domain.request.ReqCreateFilmDto;
import com.cinema.ticketbooking.domain.request.ReqUpdateFilmDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.repository.FilmRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class FilmService {
    final private FilmRepository filmRepository;

    public FilmService(FilmRepository filmRepository) {
        this.filmRepository = filmRepository;
    }

    public ResultPaginationDto getAllFilms(Specification<Film> spec, Pageable pageable) {
        Page<Film> pageFilm =  this.filmRepository.findAll(spec, pageable);
        ResultPaginationDto resultPaginationDto = new ResultPaginationDto();
        ResultPaginationDto.Meta mt = new ResultPaginationDto.Meta();

        mt.setCurrentPage(pageable.getPageNumber() + 1);
        mt.setTotalPages(pageFilm.getTotalPages());
        mt.setPageSize(pageable.getPageSize());
        mt.setTotalItems(pageFilm.getTotalElements());

        resultPaginationDto.setMeta(mt);
        resultPaginationDto.setData(pageFilm.getContent());

        return resultPaginationDto;
    }

    public Film createFilm (ReqCreateFilmDto reqFilm){
        Film film = new Film();
        film.setName(reqFilm.getName());
        film.setDuration(reqFilm.getDuration());
        film.setPrice(reqFilm.getPrice());
        film.setDescription(reqFilm.getDescription());
        film.setGenre(reqFilm.getGenre());
        film.setLanguage(reqFilm.getLanguage());
        film.setRating(reqFilm.getRating());
        film.setRelease_date(reqFilm.getRelease_date());

        return this.filmRepository.save(film);
    }

    public void deleteFilm (Long filmId){
        this.filmRepository.deleteById(filmId);
    }

    public Film updateFilm (ReqUpdateFilmDto reqFilm){
        Optional<Film> filmOptional = this.filmRepository.findById(reqFilm.getId());
        if (filmOptional.isPresent()) {
            Film newFilm =  filmOptional.get();
            newFilm.setName(reqFilm.getName());
            newFilm.setDuration(reqFilm.getDuration());
            newFilm.setPrice(reqFilm.getPrice());
            newFilm.setDescription(reqFilm.getDescription());
            newFilm.setGenre(reqFilm.getGenre());
            newFilm.setLanguage(reqFilm.getLanguage());
            newFilm.setRelease_date(reqFilm.getRelease_date());
            newFilm.setRating(reqFilm.getRating());
            return this.filmRepository.save(newFilm);

        }
        return null;
    }

    public Film getFilmById (Long id){
        Optional<Film> film = this.filmRepository.findById(id);
        if (film.isPresent()) {
            return film.get();
        }
        return null;
    }

    public boolean isFilmNameDuplicated (String filmName){
        return this.filmRepository.existsByName(filmName);
    }

}
