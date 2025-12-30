package com.cinema.ticketbooking.controller;

import com.cinema.ticketbooking.domain.Film;
import com.cinema.ticketbooking.domain.request.ReqCreateFilmDto;
import com.cinema.ticketbooking.domain.request.ReqUpdateFilmDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.service.FilmService;
import com.cinema.ticketbooking.util.error.IdInvalidException;
import com.cinema.ticketbooking.util.error.ResourceAlreadyExistsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilmControllerTest {

    @Mock
    private FilmService filmService;

    @InjectMocks
    private FilmController filmController;

    @Test
    void getAllFilms_shouldReturnResultPaginationDto_whenCalled() {
        // Arrange
        ResultPaginationDto mockResult = new ResultPaginationDto();
        when(filmService.getAllFilms(
                ArgumentMatchers.<Specification<Film>>any(),
                any(Pageable.class)
        )).thenReturn(mockResult);

        // Act
        ResponseEntity<ResultPaginationDto> response = filmController.getAllFilms(null, PageRequest.of(0, 10));

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResult, response.getBody());
        verify(filmService).getAllFilms(ArgumentMatchers.<Specification<Film>>any(), any(Pageable.class));
    }

    @Test
    void getFilm_shouldReturnFilm_whenFound() {
        // Arrange
        Film film = new Film();
        film.setId(1L);
        when(filmService.getFilmById(1L)).thenReturn(film);

        // Act
        ResponseEntity<Film> response = filmController.getFilm(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(film, response.getBody());
    }

    @Test
    void getFilm_shouldThrowIdInvalidException_whenNotFound() {
        // Arrange
        when(filmService.getFilmById(1L)).thenReturn(null);

        // Act & Assert
        IdInvalidException exception = assertThrows(
                IdInvalidException.class,
                () -> filmController.getFilm(1L)
        );
        assertEquals("film with id 1 not found", exception.getMessage());
    }

    @Test
    void createFilm_shouldReturnCreatedFilm_whenNameNotDuplicated() {
        // Arrange
        ReqCreateFilmDto request = new ReqCreateFilmDto();
        request.setName("Avengers");
        Film createdFilm = new Film();
        createdFilm.setId(1L);

        when(filmService.isFilmNameDuplicated("Avengers")).thenReturn(false);
        when(filmService.createFilm(request)).thenReturn(createdFilm);

        // Act
        ResponseEntity<Film> response = filmController.createFilm(request);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(createdFilm, response.getBody());
    }

    @Test
    void createFilm_shouldThrowResourceAlreadyExistsException_whenNameDuplicated() {
        // Arrange
        ReqCreateFilmDto request = new ReqCreateFilmDto();
        request.setName("Avengers");
        when(filmService.isFilmNameDuplicated("Avengers")).thenReturn(true);

        // Act & Assert
        ResourceAlreadyExistsException exception = assertThrows(
                ResourceAlreadyExistsException.class,
                () -> filmController.createFilm(request)
        );
        assertEquals("Film with name Avengers already exists", exception.getMessage());
    }

    @Test
    void deleteFilm_shouldReturnOk_whenFilmExists() throws Exception {
        // Arrange
        Film film = new Film();
        film.setId(1L);
        when(filmService.getFilmById(1L)).thenReturn(film);
        doNothing().when(filmService).deleteFilm(1L);

        // Act
        ResponseEntity<Void> response = filmController.deleteFilm(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        verify(filmService).deleteFilm(1L);
    }

    @Test
    void deleteFilm_shouldThrowIdInvalidException_whenFilmNotFound() {
        // Arrange
        when(filmService.getFilmById(1L)).thenReturn(null);

        // Act & Assert
        IdInvalidException exception = assertThrows(
                IdInvalidException.class,
                () -> filmController.deleteFilm(1L)
        );
        assertEquals("Film with id 1 not found", exception.getMessage());
        verify(filmService, never()).deleteFilm(anyLong());
    }

    @Test
    void updateFilm_shouldReturnUpdatedFilm_whenFilmExists() {
        // Arrange
        ReqUpdateFilmDto request = new ReqUpdateFilmDto();
        request.setId(1L);
        Film updatedFilm = new Film();
        updatedFilm.setId(1L);
        when(filmService.updateFilm(request)).thenReturn(updatedFilm);

        // Act
        ResponseEntity<Film> response = filmController.updateFilm(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedFilm, response.getBody());
    }

    @Test
    void updateFilm_shouldThrowIdInvalidException_whenFilmNotFound() {
        // Arrange
        ReqUpdateFilmDto request = new ReqUpdateFilmDto();
        request.setId(1L);
        when(filmService.updateFilm(request)).thenReturn(null);

        // Act & Assert
        IdInvalidException exception = assertThrows(
                IdInvalidException.class,
                () -> filmController.updateFilm(request)
        );
        assertEquals("Film with id 1 does not exist", exception.getMessage());
    }
}
