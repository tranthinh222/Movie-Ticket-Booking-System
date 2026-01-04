package com.cinema.ticketbooking.controller;

import com.cinema.ticketbooking.domain.User;
import com.cinema.ticketbooking.domain.request.ReqCreateBookingDto;
import com.cinema.ticketbooking.domain.response.ResCreateBookingDto;
import com.cinema.ticketbooking.service.BookingService;
import com.cinema.ticketbooking.service.UserService;
import com.cinema.ticketbooking.util.SecurityUtil;
import com.cinema.ticketbooking.util.constant.PaymentMethodEnum;
import com.cinema.ticketbooking.util.error.IdInvalidException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingControllerTest {

    @Mock
    private BookingService bookingService;

    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest httpServletRequest;

    @InjectMocks
    private BookingController bookingController;

    @Test
    void createBooking_shouldReturnCreatedBookingDto_whenUserExists() {
        // Arrange
        ReqCreateBookingDto request = new ReqCreateBookingDto();
        request.setPaymentMethod(PaymentMethodEnum.CASH);

        User user = new User();
        user.setId(1L);
        user.setUsername("TestUser");

        ResCreateBookingDto responseDto = new ResCreateBookingDto();
        responseDto.setUserId(1L);
        responseDto.setUsername("TestUser");
        responseDto.setPrice(200000.0);

        try (MockedStatic<SecurityUtil> utilities = mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getCurrentUserId).thenReturn(Optional.of(1L));

            when(userService.getUserById(1L)).thenReturn(user);
            when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
            when(bookingService.createBooking(1L, PaymentMethodEnum.CASH, "127.0.0.1")).thenReturn(responseDto);

            // Act
            ResponseEntity<ResCreateBookingDto> response = bookingController.createBooking(request, httpServletRequest);

            // Assert
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertEquals(1L, response.getBody().getUserId());
            assertEquals("TestUser", response.getBody().getUsername());
            assertEquals(200000.0, response.getBody().getPrice());
            verify(userService).getUserById(1L);
            verify(bookingService).createBooking(1L, PaymentMethodEnum.CASH, "127.0.0.1");
        }
    }

    @Test
    void createBooking_shouldThrowIdInvalidException_whenUserNotAuthenticated() {
        // Arrange
        ReqCreateBookingDto request = new ReqCreateBookingDto();
        request.setPaymentMethod(PaymentMethodEnum.CASH);

        try (MockedStatic<SecurityUtil> utilities = mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getCurrentUserId).thenReturn(Optional.empty());

            // Act & Assert
            IdInvalidException exception = assertThrows(
                    IdInvalidException.class,
                    () -> bookingController.createBooking(request, httpServletRequest)
            );

            assertEquals("User not authenticated", exception.getMessage());
            verify(bookingService, never()).createBooking(anyLong(), any(), anyString());
        }
    }

    @Test
    void createBooking_shouldThrowIdInvalidException_whenUserNotFound() {
        // Arrange
        ReqCreateBookingDto request = new ReqCreateBookingDto();
        request.setPaymentMethod(PaymentMethodEnum.CASH);

        try (MockedStatic<SecurityUtil> utilities = mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getCurrentUserId).thenReturn(Optional.of(1L));
            when(userService.getUserById(1L)).thenReturn(null);

            // Act & Assert
            IdInvalidException exception = assertThrows(
                    IdInvalidException.class,
                    () -> bookingController.createBooking(request, httpServletRequest)
            );

            assertEquals("User not found", exception.getMessage());
            verify(userService).getUserById(1L);
            verify(bookingService, never()).createBooking(anyLong(), any(), anyString());
        }
    }
}
