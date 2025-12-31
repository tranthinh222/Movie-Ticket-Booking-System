package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.Booking;
import com.cinema.ticketbooking.domain.User;
import com.cinema.ticketbooking.domain.request.ReqCreateBookingDto;
import com.cinema.ticketbooking.repository.BookingRepository;
import com.cinema.ticketbooking.util.constant.BookingStatusEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserService userService;

    @Mock
    private BookingItemService bookingItemService;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void createBooking_shouldCreateBookingWithTotalPrice_whenValidRequest() {
        // Arrange
        ReqCreateBookingDto req = new ReqCreateBookingDto();
        req.setUserId(1L);

        User user = new User();
        user.setId(1L);

        Booking savedBooking = new Booking();
        savedBooking.setId(10L);
        savedBooking.setStatus(BookingStatusEnum.PENDING);
        savedBooking.setUser(user);

        when(userService.getUserById(1L)).thenReturn(user);
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
        when(bookingItemService.createListItem(1L, savedBooking)).thenReturn(200.0);

        // Act
        Booking result = bookingService.createBooking(req);

        // Assert
        assertNotNull(result);
        assertEquals(BookingStatusEnum.PENDING, result.getStatus());
        assertEquals(200.0, result.getTotal_price());
        assertEquals(user, result.getUser());

        verify(userService).getUserById(1L);
        verify(bookingRepository).save(any(Booking.class));
        verify(bookingItemService).createListItem(1L, savedBooking);
    }
}
