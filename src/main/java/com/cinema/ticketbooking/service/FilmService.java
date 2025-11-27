package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.Film;
import com.cinema.ticketbooking.domain.dto.Meta;
import com.cinema.ticketbooking.domain.dto.ResultPaginationDto;
import com.cinema.ticketbooking.repository.FilmRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
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
        Meta mt = new Meta();

        mt.setCurrentPage(pageable.getPageNumber() + 1);
        mt.setTotalPages(pageFilm.getTotalPages());
        mt.setPageSize(pageable.getPageSize());
        mt.setTotalItems(pageFilm.getTotalElements());

        resultPaginationDto.setMeta(mt);
        resultPaginationDto.setData(pageFilm.getContent());

        return resultPaginationDto;
    }

    public Film createFilm (Film reqFilm){
        return this.filmRepository.save(reqFilm);
    }

    public void deleteFilm (Film reqFilm){
        this.filmRepository.delete(reqFilm);
    }

    public Film updateFilm (Film reqFilm){
        Optional<Film> companyOptional = this.filmRepository.findById(reqFilm.getId());
        if (companyOptional.isPresent()) {
            Film newCompany =  companyOptional.get();
            newCompany.setName(reqFilm.getName());
            newCompany.setDuration(reqFilm.getDuration());
            newCompany.setPrice(reqFilm.getPrice());
            newCompany.setDescription(reqFilm.getDescription());
            newCompany.setGenre(reqFilm.getGenre());
            newCompany.setLanguage(reqFilm.getLanguage());
            newCompany.setRelease_date(reqFilm.getRelease_date());
            newCompany.setRating(reqFilm.getRating());
            return this.filmRepository.save(reqFilm);

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

}
