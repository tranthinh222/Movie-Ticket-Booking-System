package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.Film;
import com.cinema.ticketbooking.domain.request.ReqCreateFilmDto;
import com.cinema.ticketbooking.domain.request.ReqUpdateFilmDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.repository.FilmRepository;
import com.cinema.ticketbooking.util.error.BadRequestException;
import com.cinema.ticketbooking.util.error.ResourceAlreadyExistsException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FilmService {
    final private FilmRepository filmRepository;

    public FilmService(FilmRepository filmRepository) {
        this.filmRepository = filmRepository;
    }

    public ResultPaginationDto getAllFilms(Specification<Film> spec, Pageable pageable) {
        Page<Film> pageFilm = this.filmRepository.findAll(spec, pageable);
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

    public Film createFilm(ReqCreateFilmDto reqFilm) {
        Film film = new Film();
        film.setName(reqFilm.getName());
        film.setDirector(reqFilm.getDirector());
        film.setActors(reqFilm.getActors());
        film.setDuration(reqFilm.getDuration());
        film.setPrice(reqFilm.getPrice());
        film.setDescription(reqFilm.getDescription());
        film.setGenre(reqFilm.getGenre());
        film.setLanguage(reqFilm.getLanguage());
        film.setStatus(reqFilm.getStatus());
        film.setReleaseDate(reqFilm.getRelease_date());
        film.setThumbnail(reqFilm.getThumbnail());

        return this.filmRepository.save(film);
    }

    public void deleteFilm(Long filmId) {
        this.filmRepository.deleteById(filmId);
    }

    private boolean hasUpdatableField(ReqUpdateFilmDto req) {
        return (req.getName() != null && !req.getName().trim().isEmpty())
                || req.getDirector() != null && !req.getDirector().trim().isEmpty()
                || req.getActors() != null && !req.getActors().trim().isEmpty()
                || req.getDuration() != null
                || req.getPrice() != null
                || (req.getDescription() != null && !req.getDescription().trim().isEmpty())
                || (req.getGenre() != null && !req.getGenre().trim().isEmpty())
                || (req.getLanguage() != null && !req.getLanguage().trim().isEmpty())
                || req.getRelease_date() != null
                || req.getRating() != null
                || (req.getThumbnail() != null && !req.getThumbnail().trim().isEmpty());
    }

    public Film updateFilm(ReqUpdateFilmDto reqFilm) {
        if (!hasUpdatableField(reqFilm)) {
            throw new BadRequestException("No data provided for update");
        }

        Optional<Film> filmOptional = this.filmRepository.findById(reqFilm.getId());
        if (filmOptional.isPresent()) {
            Film newFilm = filmOptional.get();

            // boolean isFilmExisted = isFilmNameDuplicated(reqFilm.getName());
            // if (isFilmExisted) {
            // throw new ResourceAlreadyExistsException(
            // "Film with name " + reqFilm.getName() + " already exists");
            // }

            Optional.ofNullable(reqFilm.getName())
                    .filter(name -> !name.trim().isEmpty())
                    .ifPresent(name -> newFilm.setName(name));

            Optional.ofNullable(reqFilm.getStatus())
                    .ifPresent(status -> newFilm.setStatus(status));

            Optional.ofNullable(reqFilm.getDirector())
                    .filter(director -> !director.trim().isEmpty())
                    .ifPresent(director -> newFilm.setDirector(director));

            Optional.ofNullable(reqFilm.getActors())
                    .filter(actors -> !actors.trim().isEmpty())
                    .ifPresent(actors -> newFilm.setActors(actors));

            Optional.ofNullable(reqFilm.getDuration())
                    .ifPresent(duration -> newFilm.setDuration(duration));

            Optional.ofNullable(reqFilm.getPrice())
                    .ifPresent(price -> newFilm.setPrice(price));

            Optional.ofNullable(reqFilm.getDescription())
                    .filter(desc -> !desc.trim().isEmpty())
                    .ifPresent(desc -> newFilm.setDescription(desc));

            Optional.ofNullable(reqFilm.getGenre())
                    .filter(genre -> !genre.trim().isEmpty())
                    .ifPresent(genre -> newFilm.setGenre(genre));

            Optional.ofNullable(reqFilm.getLanguage())
                    .filter(lang -> !lang.trim().isEmpty())
                    .ifPresent(lang -> newFilm.setLanguage(lang));

            Optional.ofNullable(reqFilm.getRelease_date())
                    .ifPresent(date -> newFilm.setReleaseDate(date));

            Optional.ofNullable(reqFilm.getThumbnail())
                    .filter(thumbnail -> !thumbnail.trim().isEmpty())
                    .ifPresent(thumbnail -> newFilm.setThumbnail(thumbnail));

            return this.filmRepository.save(newFilm);

        }
        return null;
    }

    public Film getFilmById(Long id) {
        Optional<Film> film = this.filmRepository.findById(id);
        if (film.isPresent()) {
            return film.get();
        }
        return null;
    }

    public boolean isFilmNameDuplicated(String filmName) {
        return this.filmRepository.existsByName(filmName);
    }

}
