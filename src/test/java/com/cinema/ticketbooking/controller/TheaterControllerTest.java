package com.cinema.ticketbooking.controller;

import com.cinema.ticketbooking.domain.Address;
import com.cinema.ticketbooking.domain.Theater;
import com.cinema.ticketbooking.domain.request.ReqCreateTheaterDto;
import com.cinema.ticketbooking.domain.request.ReqUpdateTheaterDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.service.AddressService;
import com.cinema.ticketbooking.service.TheaterService;
import com.cinema.ticketbooking.util.error.IdInvalidException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TheaterControllerTest {

    @Mock private TheaterService theaterService;
    @Mock private AddressService addressService;

    @InjectMocks private TheaterController theaterController;

    // -----------------------
    // GET /theaters
    // -----------------------
    @Test
    void getAllTheaters_shouldReturnOkAndBody() {
        // Arrange
        Specification<Theater> spec = null;
        Pageable pageable = PageRequest.of(0, 10);

        ResultPaginationDto dto = new ResultPaginationDto();
        when(theaterService.getAllTheaters(spec, pageable)).thenReturn(dto);

        // Act
        ResponseEntity<ResultPaginationDto> res = theaterController.getAllTheaters(spec, pageable);

        // Assert
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertSame(dto, res.getBody());
        verify(theaterService).getAllTheaters(spec, pageable);
    }

    // -----------------------
    // GET /theaters/{id}
    // -----------------------
    @Test
    void getTheater_shouldReturnOk_whenFound() {
        // Arrange
        Theater t = new Theater();
        when(theaterService.findTheaterById(1L)).thenReturn(t);

        // Act
        ResponseEntity<Theater> res = theaterController.getTheater(1L);

        // Assert
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertSame(t, res.getBody());
        verify(theaterService).findTheaterById(1L);
    }

    @Test
    void getTheater_shouldThrowIdInvalidException_whenNotFound() {
        // Arrange
        when(theaterService.findTheaterById(99L)).thenReturn(null);

        // Act + Assert
        IdInvalidException ex = assertThrows(IdInvalidException.class,
                () -> theaterController.getTheater(99L));
        assertEquals("theater with id 99 does not exist", ex.getMessage());
    }

    // -----------------------
    // POST /theaters
    // -----------------------
    @Test
    void createtheater_shouldThrow_whenAddressNotFound() {
        // Arrange
        ReqCreateTheaterDto req = new ReqCreateTheaterDto();
        req.setAddressId(10L);

        when(addressService.findAddressById(10L)).thenReturn(null);

        // Act + Assert
        IdInvalidException ex = assertThrows(IdInvalidException.class,
                () -> theaterController.createtheater(req));
        assertEquals("Address with id 10 not found", ex.getMessage());

        verify(theaterService, never()).createTheater(any());
    }

    @Test
    void createtheater_shouldReturnCreated_whenAddressExists() {
        // Arrange
        ReqCreateTheaterDto req = new ReqCreateTheaterDto();
        req.setAddressId(10L);

        when(addressService.findAddressById(10L)).thenReturn(new Address());

        Theater created = new Theater();
        when(theaterService.createTheater(req)).thenReturn(created);

        // Act
        ResponseEntity<Theater> res = theaterController.createtheater(req);

        // Assert
        assertEquals(HttpStatus.CREATED, res.getStatusCode());
        assertSame(created, res.getBody());
        verify(theaterService).createTheater(req);
    }

    // -----------------------
    // DELETE /theaters/{id}
    // -----------------------
    @Test
    void deleteTheater_shouldReturnOk_whenFound() {
        // Arrange
        when(theaterService.findTheaterById(5L)).thenReturn(new Theater());

        // Act
        ResponseEntity<Void> res = theaterController.deleteTheater(5L);

        // Assert
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertNull(res.getBody());
        verify(theaterService).deleteTheater(5L);
    }

    @Test
    void deleteTheater_shouldThrowIdInvalidException_whenNotFound() {
        // Arrange
        when(theaterService.findTheaterById(5L)).thenReturn(null);

        // Act + Assert
        IdInvalidException ex = assertThrows(IdInvalidException.class,
                () -> theaterController.deleteTheater(5L));
        assertEquals("theater with id 5 not found", ex.getMessage());

        verify(theaterService, never()).deleteTheater(anyLong());
    }

    // -----------------------
    // PUT /theaters
    // -----------------------
    @Test
    void updateTheater_shouldReturnOk_whenUpdated() {
        // Arrange
        ReqUpdateTheaterDto req = new ReqUpdateTheaterDto();
        req.setId(1L);

        Theater updated = new Theater();
        when(theaterService.updateTheater(req)).thenReturn(updated);

        // Act
        ResponseEntity<Theater> res = theaterController.updateTheater(req);

        // Assert
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertSame(updated, res.getBody());
        verify(theaterService).updateTheater(req);
    }

    @Test
    void updateTheater_shouldThrowIdInvalidException_whenNotExist() {
        // Arrange
        ReqUpdateTheaterDto req = new ReqUpdateTheaterDto();
        req.setId(99L);

        when(theaterService.updateTheater(req)).thenReturn(null);

        // Act + Assert
        IdInvalidException ex = assertThrows(IdInvalidException.class,
                () -> theaterController.updateTheater(req));
        assertEquals("Theater with id 99 does not exist", ex.getMessage());
    }
}
