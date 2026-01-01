package com.cinema.ticketbooking.controller;

import com.cinema.ticketbooking.domain.Auditorium;
import com.cinema.ticketbooking.domain.Seat;
import com.cinema.ticketbooking.domain.SeatVariant;
import com.cinema.ticketbooking.domain.request.ReqCreateSeatDto;
import com.cinema.ticketbooking.domain.request.ReqUpdateSeatDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.service.AuditoriumService;
import com.cinema.ticketbooking.service.SeatService;
import com.cinema.ticketbooking.service.SeatVariantService;
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
class SeatControllerTest {

    @Mock private SeatService seatService;
    @Mock private AuditoriumService auditoriumService;
    @Mock private SeatVariantService seatVariantService;

    @InjectMocks private SeatController seatController;

    // -----------------------
    // GET /seats
    // -----------------------
    @Test
    void getAllSeats_shouldReturnOkAndBody() {
        // Arrange
        Specification<Seat> spec = null;
        Pageable pageable = PageRequest.of(0, 10);

        ResultPaginationDto dto = new ResultPaginationDto();
        when(seatService.getAllSeats(spec, pageable)).thenReturn(dto);

        // Act
        ResponseEntity<ResultPaginationDto> res = seatController.getAllSeats(spec, pageable);

        // Assert
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertSame(dto, res.getBody());
        verify(seatService).getAllSeats(spec, pageable);
    }

    // -----------------------
    // GET /seats/{id}
    // -----------------------
    @Test
    void getSeatById_shouldReturnOk_whenFound() {
        // Arrange
        Seat seat = new Seat();
        when(seatService.findSeatById(1L)).thenReturn(seat);

        // Act
        ResponseEntity<Seat> res = seatController.getSeatById(1L);

        // Assert
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertSame(seat, res.getBody());
        verify(seatService).findSeatById(1L);
    }

    @Test
    void getSeatById_shouldThrowIdInvalidException_whenNotFound() {
        // Arrange
        when(seatService.findSeatById(99L)).thenReturn(null);

        // Act + Assert
        IdInvalidException ex = assertThrows(IdInvalidException.class,
                () -> seatController.getSeatById(99L));
        assertEquals("Seat with id 99 not found", ex.getMessage());
    }

    // -----------------------
    // DELETE /seats/{id}
    // -----------------------
    @Test
    void deleteSeat_shouldReturnOkAndCallDelete_whenFound() {
        // Arrange
        Seat seat = new Seat();
        when(seatService.findSeatById(5L)).thenReturn(seat);

        // Act
        ResponseEntity<Void> res = seatController.deleteSeat(5L);

        // Assert
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertNull(res.getBody());
        verify(seatService).deleteSeat(5L);
    }

    @Test
    void deleteSeat_shouldThrowIdInvalidException_whenNotFound() {
        // Arrange
        when(seatService.findSeatById(5L)).thenReturn(null);

        // Act + Assert
        IdInvalidException ex = assertThrows(IdInvalidException.class,
                () -> seatController.deleteSeat(5L));
        assertEquals("seat with id 5 not found", ex.getMessage());

        verify(seatService, never()).deleteSeat(anyLong());
    }

    // -----------------------
    // POST /seats
    // -----------------------
    @Test
    void createSeat_shouldThrow_whenAuditoriumNotFound() {
        // Arrange
        ReqCreateSeatDto req = new ReqCreateSeatDto();
        req.setAuditoriumId(10L);
        req.setSeatVariantId(20L);

        when(auditoriumService.getAuditoriumById(10L)).thenReturn(null);

        // Act + Assert
        IdInvalidException ex = assertThrows(IdInvalidException.class,
                () -> seatController.createSeat(req));
        assertEquals("Auditorium with id 10 not found", ex.getMessage());

        verify(seatService, never()).createSeat(any());
    }

    @Test
    void createSeat_shouldThrow_whenSeatVariantNotFound() {
        // Arrange
        ReqCreateSeatDto req = new ReqCreateSeatDto();
        req.setAuditoriumId(10L);
        req.setSeatVariantId(20L);

        when(auditoriumService.getAuditoriumById(10L)).thenReturn(new Auditorium());
        when(seatVariantService.findSeatVariantById(20L)).thenReturn(null);

        // Act + Assert
        IdInvalidException ex = assertThrows(IdInvalidException.class,
                () -> seatController.createSeat(req));
        assertEquals("SeatVariant with id 20 not found", ex.getMessage());

        verify(seatService, never()).createSeat(any());
    }

    @Test
    void createSeat_shouldReturnCreatedAndCallService_whenValid() {
        // Arrange
        ReqCreateSeatDto req = new ReqCreateSeatDto();
        req.setAuditoriumId(10L);
        req.setSeatVariantId(20L);

        when(auditoriumService.getAuditoriumById(10L)).thenReturn(new Auditorium());
        when(seatVariantService.findSeatVariantById(20L)).thenReturn(new SeatVariant());

        Seat created = new Seat();
        when(seatService.createSeat(req)).thenReturn(created);

        // Act
        ResponseEntity<Seat> res = seatController.createSeat(req);

        // Assert
        assertEquals(HttpStatus.CREATED, res.getStatusCode());
        assertSame(created, res.getBody());
        verify(seatService).createSeat(req);
    }

    // -----------------------
    // PUT /seats
    // -----------------------
    @Test
    void updateSeat_shouldReturnOk_whenUpdatedSuccessfully() {
        // Arrange
        ReqUpdateSeatDto req = new ReqUpdateSeatDto();
        req.setId(1L);

        Seat updated = new Seat();
        updated.setId(1L);
        when(seatService.updateSeat(req)).thenReturn(updated);

        // Act
        ResponseEntity<Seat> res = seatController.updateSeat(req);

        // Assert
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertSame(updated, res.getBody());
        verify(seatService).updateSeat(req);
    }
}
