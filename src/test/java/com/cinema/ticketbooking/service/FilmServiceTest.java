package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.Film;
import com.cinema.ticketbooking.domain.request.ReqCreateFilmDto;
import com.cinema.ticketbooking.domain.request.ReqUpdateFilmDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.repository.FilmRepository;
import com.cinema.ticketbooking.util.error.BadRequestException;
import com.cinema.ticketbooking.util.error.ResourceAlreadyExistsException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilmServiceTest {

        @Mock
        private FilmRepository filmRepository;

        @InjectMocks
        private FilmService filmService;

        // getAllFilms
        @Test
        void getAllFilms_shouldReturnPaginationResult_whenCalled() {
                // Arrange
                Film film = new Film();
                Page<Film> page = new PageImpl<>(List.of(film));
                Pageable pageable = PageRequest.of(0, 10);

                when(filmRepository.findAll(
                                ArgumentMatchers.<Specification<Film>>any(),
                                eq(pageable))).thenReturn(page);

                // Act
                ResultPaginationDto result = filmService.getAllFilms(null, pageable);

                // Assert
                assertNotNull(result);
                assertEquals(1, result.getMeta().getTotalItems());
                List<?> data = (List<?>) result.getData();
                assertEquals(1, data.size());
                verify(filmRepository).findAll(ArgumentMatchers.<Specification<Film>>any(), eq(pageable));
        }

        // createFilm
        @Test
        void createFilm_shouldSaveAndReturnFilm_whenValidRequest() {
                // Arrange
                ReqCreateFilmDto req = new ReqCreateFilmDto();
                req.setName("Avatar");

                Film savedFilm = new Film();
                savedFilm.setName("Avatar");

                when(filmRepository.save(any(Film.class)))
                                .thenReturn(savedFilm);

                // Act
                Film result = filmService.createFilm(req);

                // Assert
                assertNotNull(result);
                assertEquals("Avatar", result.getName());
                verify(filmRepository).save(any(Film.class));
        }

        // deleteFilm
        @Test
        void deleteFilm_shouldCallRepositoryDelete_whenCalled() {
                // Arrange
                Long filmId = 1L;

                // Act
                filmService.deleteFilm(filmId);

                // Assert
                verify(filmRepository).deleteById(filmId);
        }

        // updateFilm
        @Test
        void updateFilm_shouldThrowBadRequestException_whenNoFieldProvided() {
                // Arrange
                ReqUpdateFilmDto req = new ReqUpdateFilmDto();
                req.setId(1L);

                // Act & Assert
                assertThrows(
                                BadRequestException.class,
                                () -> filmService.updateFilm(req));
        }

        // Test removed: updateFilm no longer checks for duplicate names
        // The duplicate name validation was removed from FilmService.updateFilm()

        @Test
        void updateFilm_shouldUpdateAndReturnFilm_whenValidRequest() {
                // Arrange
                ReqUpdateFilmDto req = new ReqUpdateFilmDto();
                req.setId(1L);
                req.setName("New Name");

                Film film = new Film();
                film.setName("Old Name");

                when(filmRepository.findById(1L))
                                .thenReturn(Optional.of(film));
                when(filmRepository.save(any(Film.class)))
                                .thenAnswer(invocation -> invocation.getArgument(0));

                // Act
                Film result = filmService.updateFilm(req);

                // Assert
                assertNotNull(result);
                assertEquals("New Name", result.getName());
                verify(filmRepository).save(film);
        }

        @Test
        void updateFilm_shouldReturnNull_whenFilmNotFound() {
                // Arrange
                ReqUpdateFilmDto req = new ReqUpdateFilmDto();
                req.setId(1L);
                req.setName("Test");

                when(filmRepository.findById(1L))
                                .thenReturn(Optional.empty());

                // Act
                Film result = filmService.updateFilm(req);

                // Assert
                assertNull(result);
        }

        // getFilmById
        @Test
        void getFilmById_shouldReturnFilm_whenFound() {
                // Arrange
                Film film = new Film();
                when(filmRepository.findById(1L))
                                .thenReturn(Optional.of(film));

                // Act
                Film result = filmService.getFilmById(1L);

                // Assert
                assertNotNull(result);
        }

        @Test
        void getFilmById_shouldReturnNull_whenNotFound() {
                // Arrange
                when(filmRepository.findById(1L))
                                .thenReturn(Optional.empty());

                // Act
                Film result = filmService.getFilmById(1L);

                // Assert
                assertNull(result);
        }

        // isFilmNameDuplicated
        @Test
        void isFilmNameDuplicated_shouldReturnTrue_whenFilmExists() {
                // Arrange
                when(filmRepository.existsByName("Avatar"))
                                .thenReturn(true);

                // Act
                boolean result = filmService.isFilmNameDuplicated("Avatar");

                // Assert
                assertTrue(result);
        }
}
