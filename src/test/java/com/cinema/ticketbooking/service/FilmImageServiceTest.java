package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.Film;
import com.cinema.ticketbooking.domain.FilmImage;
import com.cinema.ticketbooking.domain.response.ResFilmImage;
import com.cinema.ticketbooking.repository.FilmImageRepository;
import com.cinema.ticketbooking.repository.FilmRepository;
import com.cinema.ticketbooking.util.error.IdInvalidException;
import com.cloudinary.Cloudinary;
import com.cloudinary.Uploader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilmImageServiceTest {

    @Mock
    private Cloudinary cloudinary;

    @Mock
    private Uploader uploader;

    @Mock
    private FilmRepository filmRepository;

    @Mock
    private FilmImageRepository filmImageRepository;

    @InjectMocks
    private FilmImageService filmImageService;

    // getImageByFilm 
    @Test
    void getImageByFilm_shouldReturnResFilmImage_whenImageExists() {
        // Arrange
        FilmImage image = new FilmImage();
        image.setId(1L);
        image.setUrl("http://image.url");

        when(filmImageRepository.findByFilmId(1L))
                .thenReturn(Optional.of(image));

        // Act
        ResFilmImage result = filmImageService.getImageByFilm(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("http://image.url", result.getUrl());
    }

    @Test
    void getImageByFilm_shouldThrowException_whenImageNotFound() {
        // Arrange
        when(filmImageRepository.findByFilmId(1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class,
                () -> filmImageService.getImageByFilm(1L));
    }

    // uploadOrReplace
    @Test
    void uploadOrReplace_shouldDeleteOldImageAndSaveNew_whenOldExists() throws IOException {
        // Arrange
        Film film = new Film();
        film.setId(1L);

        FilmImage oldImage = new FilmImage();
        oldImage.setPublicId("old_public_id");

        MultipartFile file = mock(MultipartFile.class);

        when(file.getBytes()).thenReturn(new byte[]{1, 2, 3});
        when(filmImageRepository.findByFilmId(1L))
                .thenReturn(Optional.of(oldImage));

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap()))
                .thenReturn(Map.of(
                        "public_id", "new_public_id",
                        "secure_url", "http://new.url"
                ));

        when(filmImageRepository.save(any(FilmImage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        FilmImage result = filmImageService.uploadOrReplace(film, file);

        // Assert
        assertNotNull(result);
        assertEquals("new_public_id", result.getPublicId());
        assertEquals("http://new.url", result.getUrl());

        verify(uploader).destroy(eq("old_public_id"), anyMap());
        verify(filmImageRepository).delete(oldImage);
        verify(filmImageRepository).save(any(FilmImage.class));
    }

    @Test
    void uploadOrReplace_shouldSaveNewImage_whenNoOldImage() throws IOException {
        // Arrange
        Film film = new Film();
        film.setId(1L);

        MultipartFile file = mock(MultipartFile.class);
        when(file.getBytes()).thenReturn(new byte[]{1});

        when(filmImageRepository.findByFilmId(1L))
                .thenReturn(Optional.empty());

        when(cloudinary.uploader()).thenReturn(uploader);
        when(uploader.upload(any(byte[].class), anyMap()))
                .thenReturn(Map.of(
                        "public_id", "pid",
                        "secure_url", "url"
                ));

        when(filmImageRepository.save(any(FilmImage.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        FilmImage result = filmImageService.uploadOrReplace(film, file);

        // Assert
        assertEquals("pid", result.getPublicId());
        assertEquals("url", result.getUrl());
        verify(filmImageRepository, never()).delete(any());
    }

    // deleteByFilmImageId
    @Test
    void deleteByFilmImageId_shouldDeleteImage_whenExists() throws IOException{
        // Arrange
        FilmImage image = new FilmImage();
        image.setPublicId("pid");

        when(filmImageRepository.findByFilmId(1L))
                .thenReturn(Optional.of(image));
        when(cloudinary.uploader()).thenReturn(uploader);

        // Act
        filmImageService.deleteByFilmImageId(1L);

        // Assert
        verify(uploader).destroy(eq("pid"), anyMap());
        verify(filmImageRepository).delete(image);
    }

    @Test
    void deleteByFilmImageId_shouldThrowException_whenNotFound() {
        // Arrange
        when(filmImageRepository.findByFilmId(1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IdInvalidException.class,
                () -> filmImageService.deleteByFilmImageId(1L));
    }
}
