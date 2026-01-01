package com.cinema.ticketbooking.controller;

import com.cinema.ticketbooking.domain.Film;
import com.cinema.ticketbooking.domain.FilmImage;
import com.cinema.ticketbooking.domain.response.ResFilmImage;
import com.cinema.ticketbooking.service.FilmService;
import com.cinema.ticketbooking.util.error.IdInvalidException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilmImageControllerTest {

    @Mock
    private FilmImageService filmImageService;

    @Mock
    private FilmService filmService;

    @InjectMocks
    private FilmImageController filmImageController;

    @Test
    void getFilmImage_shouldReturnResFilmImage_whenFilmExists() {
        // Arrange
        ResFilmImage resFilmImage = new ResFilmImage();
        when(filmImageService.getImageByFilm(1L)).thenReturn(resFilmImage);

        // Act
        ResFilmImage result = filmImageController.getFilmImage(1L);

        // Assert
        assertEquals(resFilmImage, result);
        verify(filmImageService).getImageByFilm(1L);
    }

    @Test
    void uploadOrReplaceImage_shouldReturnMap_whenFilmExists() throws IOException {
        // Arrange
        Film film = new Film();
        film.setId(1L);
        when(filmService.getFilmById(1L)).thenReturn(film);

        FilmImage image = new FilmImage();
        image.setId(100L);
        image.setUrl("http://example.com/image.jpg");
        when(filmImageService.uploadOrReplace(eq(film), any(MultipartFile.class))).thenReturn(image);

        MockMultipartFile file = new MockMultipartFile("file", "image.jpg", "image/jpeg", "data".getBytes());

        // Act
        ResponseEntity<?> response = filmImageController.uploadOrReplaceImage(1L, file);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals(100L, body.get("imageId"));
        assertEquals("http://example.com/image.jpg", body.get("url"));
        verify(filmService).getFilmById(1L);
        verify(filmImageService).uploadOrReplace(eq(film), any(MultipartFile.class));
    }

    @Test
    void uploadOrReplaceImage_shouldThrowIdInvalidException_whenFilmNotFound() throws IOException {
        // Arrange
        when(filmService.getFilmById(1L)).thenReturn(null);
        MockMultipartFile file = new MockMultipartFile("file", "image.jpg", "image/jpeg", "data".getBytes());

        // Act & Assert
        IdInvalidException exception = assertThrows(
                IdInvalidException.class,
                () -> filmImageController.uploadOrReplaceImage(1L, file)
        );

        assertEquals("film with id 1 not found", exception.getMessage());
        verify(filmService).getFilmById(1L);
        verify(filmImageService, never()).uploadOrReplace(any(), any());
    }

    @Test
    void deleteFilmImage_shouldReturnMessage_whenCalled() {
        // Arrange
        doNothing().when(filmImageService).deleteByFilmImageId(1L);

        // Act
        ResponseEntity<?> response = filmImageController.deleteFilmImage(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Film image deleted successfully", body.get("message"));
        verify(filmImageService).deleteByFilmImageId(1L);
    }
}
