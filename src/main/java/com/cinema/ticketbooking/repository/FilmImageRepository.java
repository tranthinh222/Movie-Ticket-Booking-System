package com.cinema.ticketbooking.repository;

import com.cinema.ticketbooking.domain.FilmImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FilmImageRepository
        extends JpaRepository<FilmImage, Long> {

    Optional<FilmImage> findByFilmId(Long filmId);
}

