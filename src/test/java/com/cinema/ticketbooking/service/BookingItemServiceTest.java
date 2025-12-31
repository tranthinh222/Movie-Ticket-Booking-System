package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.*;
import com.cinema.ticketbooking.repository.BookingItemRepository;
import com.cinema.ticketbooking.util.error.NoResourceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingItemServiceTest {

    @Mock
    private BookingItemRepository bookingItemRepository;

    @Mock
    private SeatHoldService seatHoldService;

    @InjectMocks
    private BookingItemService bookingItemService;

    @Test
    void createListItem_shouldThrowException_whenSeatHoldListIsEmpty() {
        // Arrange
        Long userId = 1L;
        Booking booking = new Booking();

        when(seatHoldService.getSeatHoldByUserId(userId))
                .thenReturn(List.of());

        // Act & Assert
        assertThrows(
                NoResourceException.class,
                () -> bookingItemService.createListItem(userId, booking)
        );

        verify(bookingItemRepository, never()).save(any());
    }

    @Test
    void createListItem_shouldReturnTotalPrice_whenSeatHoldExists() {
        // Arrange
        Long userId = 1L;
        Booking booking = new Booking();

        SeatVariant seatVariant = new SeatVariant();
        seatVariant.setBasePrice(100.0);
        seatVariant.setBonus(20.0);

        Seat seat = new Seat();
        seat.setSeatVariant(seatVariant);

        SeatHold seatHold = new SeatHold();
        seatHold.setSeat(seat);

        when(seatHoldService.getSeatHoldByUserId(userId))
                .thenReturn(List.of(seatHold));

        when(bookingItemRepository.save(any(BookingItem.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Double totalPrice = bookingItemService.createListItem(userId, booking);

        // Assert
        assertEquals(120.0, totalPrice);
        verify(bookingItemRepository, times(1)).save(any(BookingItem.class));
    }
}
