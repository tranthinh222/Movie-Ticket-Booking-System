package com.cinema.ticketbooking.controller;

import com.cinema.ticketbooking.domain.SeatHold;
import com.cinema.ticketbooking.domain.User;
import com.cinema.ticketbooking.domain.request.ReqCreateSeatHoldDto;
import com.cinema.ticketbooking.service.SeatHoldService;
import com.cinema.ticketbooking.service.SeatService;
import com.cinema.ticketbooking.service.UserService;
import com.cinema.ticketbooking.util.error.IdInvalidException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeatHoldControllerTest {

    @Mock private SeatHoldService seatHoldService;
    @Mock private SeatService seatService;
    @Mock private UserService userService;

    @InjectMocks private SeatHoldController seatHoldController;

    // -----------------------
    // POST /seat-holds
    // -----------------------
    @Test
    void createSeatHold_shouldReturnCreated_whenValidRequest() {
        // Arrange
        ReqCreateSeatHoldDto req = new ReqCreateSeatHoldDto();
        req.setSeatIds(List.of(100L, 101L));
        req.setShowtimeId(10L);

        List<SeatHold> createdHolds = List.of(new SeatHold(), new SeatHold());
        when(seatHoldService.createSeatHold(req)).thenReturn(createdHolds);

        // Act
        ResponseEntity<Void> res = seatHoldController.createSeatHold(req);

        // Assert
        assertEquals(HttpStatus.CREATED, res.getStatusCode());
        assertNull(res.getBody());

        verify(seatHoldService).createSeatHold(req);
    }

    // -----------------------
    // DELETE /seat-holds
    // -----------------------
    @Test
    void removeSeatHold_shouldReturnNoContent() {
        // Act
        ResponseEntity<Void> res = seatHoldController.removeSeatHold();

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, res.getStatusCode());
        assertNull(res.getBody());

        verify(seatHoldService).removeSeatHold();
    }

    // -----------------------
    // GET /seat-holds/{id}
    // -----------------------
    @Test
    void getSeatHoldsByUserId_shouldThrowIdInvalidException_whenUserNotFound() {
        // Arrange
        when(userService.getUserById(1L)).thenReturn(null);

        // Act + Assert
        IdInvalidException ex = assertThrows(IdInvalidException.class,
                () -> seatHoldController.getMethodName(1L));
        assertEquals("id user không hợp lệ", ex.getMessage());

        verify(seatHoldService, never()).getSeatHoldByUserId(anyLong());
    }

    @Test
    void getSeatHoldsByUserId_shouldReturnOkAndList_whenUserExists() {
        // Arrange
        User u = new User();
        u.setId(1);

        List<SeatHold> list = List.of(new SeatHold(), new SeatHold());

        when(userService.getUserById(1L)).thenReturn(u);
        when(seatHoldService.getSeatHoldByUserId(1L)).thenReturn(list);

        // Act
        ResponseEntity<List<SeatHold>> res = seatHoldController.getMethodName(1L);

        // Assert
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertSame(list, res.getBody());

        verify(seatHoldService).getSeatHoldByUserId(1L);
    }
}
