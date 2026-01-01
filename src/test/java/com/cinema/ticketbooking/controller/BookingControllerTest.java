package com.cinema.ticketbooking.controller;

import com.cinema.ticketbooking.domain.Booking;
import com.cinema.ticketbooking.domain.User;
import com.cinema.ticketbooking.domain.request.ReqCreateBookingDto;
import com.cinema.ticketbooking.service.BookingService;
import com.cinema.ticketbooking.service.UserService;
import com.cinema.ticketbooking.util.error.IdInvalidException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    @Mock
    private BookingService bookingService;

    @Mock
    private UserService userService;

    @InjectMocks
    private BookingController bookingController;

    @Test
    void createBooking_shouldReturnCreatedBooking_whenUserExists() {
        // Arrange
        ReqCreateBookingDto request = new ReqCreateBookingDto();
        request.setUserId(1L);

        User user = new User();
        user.setId(1L);

        Booking createdBooking = new Booking();
        createdBooking.setId(100L);

        when(userService.getUserById(1L)).thenReturn(user);
        when(bookingService.createBooking(request)).thenReturn(createdBooking);

        // Act
        ResponseEntity<Booking> response = bookingController.createBooking(request);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(createdBooking, response.getBody());
        verify(userService).getUserById(1L);
        verify(bookingService).createBooking(request);
    }

    @Test
    void createBooking_shouldThrowIdInvalidException_whenUserDoesNotExist() {
        // Arrange
        ReqCreateBookingDto request = new ReqCreateBookingDto();
        request.setUserId(1L);

        when(userService.getUserById(1L)).thenReturn(null);

        // Act & Assert
        IdInvalidException exception = assertThrows(
                IdInvalidException.class,
                () -> bookingController.createBooking(request)
        );

        assertEquals("user id khong ton tai", exception.getMessage());
        verify(userService).getUserById(1L);
        verify(bookingService, never()).createBooking(any());
    }
}
