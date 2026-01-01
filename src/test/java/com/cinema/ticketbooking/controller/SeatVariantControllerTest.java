package com.cinema.ticketbooking.controller;

import com.cinema.ticketbooking.domain.SeatVariant;
import com.cinema.ticketbooking.domain.request.ReqCreateSeatVariantDto;
import com.cinema.ticketbooking.domain.request.ReqUpdateSeatVariantDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
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
class SeatVariantControllerTest {

    @Mock private SeatVariantService seatVariantService;

    @InjectMocks private SeatVariantController seatVariantController;

    // -----------------------
    // GET /seat-variants
    // -----------------------
    @Test
    void getAllSeatVariants_shouldReturnOkAndBody() {
        // Arrange
        Specification<SeatVariant> spec = null;
        Pageable pageable = PageRequest.of(0, 10);

        ResultPaginationDto dto = new ResultPaginationDto();
        when(seatVariantService.getAllSeatVariants(spec, pageable)).thenReturn(dto);

        // Act
        ResponseEntity<ResultPaginationDto> res = seatVariantController.getAllSeatVariants(spec, pageable);

        // Assert
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertSame(dto, res.getBody());
        verify(seatVariantService).getAllSeatVariants(spec, pageable);
    }

    // -----------------------
    // GET /seat-variants/{id}
    // -----------------------
    @Test
    void getSeatVariantById_shouldReturnOk_whenFound() {
        // Arrange
        SeatVariant sv = new SeatVariant();
        when(seatVariantService.findSeatVariantById(1L)).thenReturn(sv);

        // Act
        ResponseEntity<SeatVariant> res = seatVariantController.getSeatVariantById(1L);

        // Assert
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertSame(sv, res.getBody());
        verify(seatVariantService).findSeatVariantById(1L);
    }

    @Test
    void getSeatVariantById_shouldThrowIdInvalidException_whenNotFound() {
        // Arrange
        when(seatVariantService.findSeatVariantById(99L)).thenReturn(null);

        // Act + Assert
        IdInvalidException ex = assertThrows(IdInvalidException.class,
                () -> seatVariantController.getSeatVariantById(99L));
        assertEquals("Seat variant with id 99 not found", ex.getMessage());
    }

    // -----------------------
    // POST /seat-variants
    // -----------------------
    @Test
    void createSeatVariant_shouldReturnCreatedAndBody() {
        // Arrange
        ReqCreateSeatVariantDto req = new ReqCreateSeatVariantDto();
        SeatVariant created = new SeatVariant();

        when(seatVariantService.createSeatVariant(req)).thenReturn(created);

        // Act
        ResponseEntity<SeatVariant> res = seatVariantController.createSeatVariant(req);

        // Assert
        assertEquals(HttpStatus.CREATED, res.getStatusCode());
        assertSame(created, res.getBody());
        verify(seatVariantService).createSeatVariant(req);
    }

    // -----------------------
    // DELETE /seat-variants/{id}
    // -----------------------
    @Test
    void deleteSeatVariant_shouldReturnOk_whenFound() {
        // Arrange
        when(seatVariantService.findSeatVariantById(5L)).thenReturn(new SeatVariant());

        // Act
        ResponseEntity<Void> res = seatVariantController.deleteSeatVariant(5L);

        // Assert
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertNull(res.getBody());
        verify(seatVariantService).deleteSeatVariant(5L);
    }

    @Test
    void deleteSeatVariant_shouldThrowIdInvalidException_whenNotFound() {
        // Arrange
        when(seatVariantService.findSeatVariantById(5L)).thenReturn(null);

        // Act + Assert
        IdInvalidException ex = assertThrows(IdInvalidException.class,
                () -> seatVariantController.deleteSeatVariant(5L));
        assertEquals("seat with id 5 not found", ex.getMessage());

        verify(seatVariantService, never()).deleteSeatVariant(anyLong());
    }

    // -----------------------
    // PUT /seat-variants
    // -----------------------
    @Test
    void updateSeat_shouldReturnOk_whenUpdated() {
        // Arrange
        ReqUpdateSeatVariantDto req = new ReqUpdateSeatVariantDto();
        // req.setId(1L); // optional

        SeatVariant updated = new SeatVariant();
        updated.setId(1L);

        when(seatVariantService.updateSeatVariant(req)).thenReturn(updated);

        // Act
        ResponseEntity<SeatVariant> res = seatVariantController.updateSeat(req);

        // Assert
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertSame(updated, res.getBody());
        verify(seatVariantService).updateSeatVariant(req);
    }
}
