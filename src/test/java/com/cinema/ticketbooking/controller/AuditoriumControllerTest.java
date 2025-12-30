package com.cinema.ticketbooking.controller;

import com.cinema.ticketbooking.domain.Auditorium;
import com.cinema.ticketbooking.domain.Theater;
import com.cinema.ticketbooking.domain.request.ReqCreateAuditoriumDto;
import com.cinema.ticketbooking.domain.request.ReqUpdateAuditoriumDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.service.AuditoriumService;
import com.cinema.ticketbooking.service.TheaterService;
import com.cinema.ticketbooking.util.error.IdInvalidException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentMatchers;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class AuditoriumControllerTest {

    @Mock
    private AuditoriumService auditoriumService;

    @Mock
    private TheaterService theaterService;

    @InjectMocks
    private AuditoriumController auditoriumController;

    @Test
    void getAlls_shouldReturnOk_whenCalledSuccessfully() {
        // Arrange
        ResultPaginationDto result = new ResultPaginationDto();
        when(auditoriumService.getAllAuditoriums(
                ArgumentMatchers.<Specification<Auditorium>>any(),
                any(Pageable.class))
        ).thenReturn(result);

        // Act
        ResponseEntity<ResultPaginationDto> response =
                auditoriumController.getAlls(null, PageRequest.of(0, 10));

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(result, response.getBody());
        verify(auditoriumService).getAllAuditoriums(
                ArgumentMatchers.<Specification<Auditorium>>any(),
                any(Pageable.class)
        );
    }

    @Test
    void getAuditorium_shouldReturnAuditorium_whenFound() {
        // Arrange
        Auditorium auditorium = new Auditorium();
        auditorium.setId(1L);
        when(auditoriumService.getAuditoriumById(1L))
                .thenReturn(auditorium);

        // Act
        ResponseEntity<Auditorium> response =
                auditoriumController.getAuditorium(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(auditorium, response.getBody());
    }

    @Test
    void getAuditorium_shouldThrowException_whenNotFound() {
        // Arrange
        when(auditoriumService.getAuditoriumById(1L))
                .thenReturn(null);

        // Act & Assert
        IdInvalidException exception = assertThrows(
                IdInvalidException.class,
                () -> auditoriumController.getAuditorium(1L)
        );

        assertEquals("Auditorium with id 1 not found", exception.getMessage());
    }

    @Test
    void createAuditorium_shouldReturnCreatedAuditorium_whenTheaterExists() throws Exception {
        // Arrange
        ReqCreateAuditoriumDto request = new ReqCreateAuditoriumDto();
        request.setTheaterId(1L);

        Theater theater = new Theater();
        theater.setId(1L);

        Auditorium createdAuditorium = new Auditorium();
        createdAuditorium.setId(1L);

        when(theaterService.findTheaterById(1L)).thenReturn(theater);
        when(auditoriumService.createAuditorium(request))
                .thenReturn(createdAuditorium);

        // Act
        ResponseEntity<Auditorium> response =
                auditoriumController.createAuditorium(request);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(createdAuditorium, response.getBody());
    }

    @Test
    void createAuditorium_shouldThrowException_whenTheaterNotFound() {
        // Arrange
        ReqCreateAuditoriumDto request = new ReqCreateAuditoriumDto();
        request.setTheaterId(1L);

        when(theaterService.findTheaterById(1L)).thenReturn(null);

        // Act & Assert
        IdInvalidException exception = assertThrows(
                IdInvalidException.class,
                () -> auditoriumController.createAuditorium(request)
        );

        assertEquals("theater with id 1 not found", exception.getMessage());
    }

    @Test
    void deleteAuditorium_shouldReturnOk_whenAuditoriumExists() throws Exception {
        // Arrange
        Auditorium auditorium = new Auditorium();
        auditorium.setId(1L);

        when(auditoriumService.getAuditoriumById(1L)).thenReturn(auditorium);
        doNothing().when(auditoriumService).deleteAuditorium(1L);

        // Act
        ResponseEntity<Void> response =
                auditoriumController.deleteAuditorium(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        verify(auditoriumService).deleteAuditorium(1L);
    }

    @Test
    void deleteAuditorium_shouldThrowException_whenAuditoriumNotFound() {
        // Arrange
        when(auditoriumService.getAuditoriumById(1L)).thenReturn(null);

        // Act & Assert
        Exception exception = assertThrows(
                Exception.class,
                () -> auditoriumController.deleteAuditorium(1L)
        );

        assertEquals("auditorium with id 1 not found", exception.getMessage());
    }

    @Test
    void updateAuditorium_shouldReturnUpdatedAuditorium_whenAuditoriumExists() throws Exception {
        // Arrange
        ReqUpdateAuditoriumDto request = new ReqUpdateAuditoriumDto();
        request.setId(1L);

        Auditorium updatedAuditorium = new Auditorium();
        updatedAuditorium.setId(1L);

        when(auditoriumService.updateAuditorium(request))
                .thenReturn(updatedAuditorium);

        // Act
        ResponseEntity<Auditorium> response =
                auditoriumController.updateAuditorium(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedAuditorium, response.getBody());
    }

    @Test
    void updateAuditorium_shouldThrowException_whenAuditoriumDoesNotExist() {
        // Arrange
        ReqUpdateAuditoriumDto request = new ReqUpdateAuditoriumDto();
        request.setId(1L);

        when(auditoriumService.updateAuditorium(request)).thenReturn(null);

        // Act & Assert
        Exception exception = assertThrows(
                Exception.class,
                () -> auditoriumController.updateAuditorium(request)
        );

        assertEquals("auditorium with id 1 does not exist", exception.getMessage());
    }
}
