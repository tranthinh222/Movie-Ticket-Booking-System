package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.Seat;
import com.cinema.ticketbooking.domain.SeatHold;
import com.cinema.ticketbooking.domain.ShowTime;
import com.cinema.ticketbooking.domain.User;
import com.cinema.ticketbooking.domain.request.ReqCreateSeatHoldDto;
import com.cinema.ticketbooking.repository.BookingItemRepository;
import com.cinema.ticketbooking.repository.SeatHoldRepository;
import com.cinema.ticketbooking.repository.SeatRepository;
import com.cinema.ticketbooking.repository.ShowTimeRepository;
import com.cinema.ticketbooking.repository.UserRepository;
import com.cinema.ticketbooking.util.SecurityUtil;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeatHoldServiceTest {

    @Mock private SeatHoldRepository seatHoldRepository;
    @Mock private UserRepository userRepository;
    @Mock private ShowTimeRepository showTimeRepository;
    @Mock private SeatRepository seatRepository;
    @Mock private BookingItemRepository bookingItemRepository;

    @InjectMocks private SeatHoldService seatHoldService;

    @Test
    void createSeatHold_shouldCreateSeatHolds_whenAuthenticatedAndDataValid() {
        // Arrange
        String email = "test@gmail.com";

        ReqCreateSeatHoldDto req = new ReqCreateSeatHoldDto();
        req.setSeatIds(List.of(100L));
        req.setShowtimeId(10L);

        User user = new User();
        user.setId(1L);

        Seat seat = new Seat();
        seat.setId(100L);

        ShowTime showTime = new ShowTime();
        showTime.setId(10L);

        Instant start = Instant.now();

        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserLogin).thenReturn(Optional.of(email));

            when(userRepository.findUserByEmail(email)).thenReturn(user);
            when(showTimeRepository.findById(10L)).thenReturn(Optional.of(showTime));
            when(seatRepository.lockSeats(List.of(100L))).thenReturn(List.of(seat));
            when(seatHoldRepository.existsBySeatIdAndShowTimeId(100L, 10L)).thenReturn(false);
            when(bookingItemRepository.existsBySeatIdAndShowTimeId(100L, 10L)).thenReturn(false);

            // Act
            List<SeatHold> result = seatHoldService.createSeatHold(req);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            
            SeatHold seatHold = result.get(0);
            assertEquals(user, seatHold.getUser());
            assertEquals(seat, seatHold.getSeat());
            assertEquals(showTime, seatHold.getShowTime());

            // SeatHold được save
            verify(seatHoldRepository).saveAll(anyList());

            assertNotNull(seatHold.getExpiresAt());
            Instant min = start.plus(4, ChronoUnit.MINUTES);
            Instant max = start.plus(6, ChronoUnit.MINUTES);
            assertTrue(seatHold.getExpiresAt().isAfter(min));
            assertTrue(seatHold.getExpiresAt().isBefore(max));
        }
    }

    @Test
    void createSeatHold_shouldThrowUnauthenticated_whenNoLogin() {
        // Arrange
        ReqCreateSeatHoldDto req = new ReqCreateSeatHoldDto();
        req.setSeatIds(List.of(100L));
        req.setShowtimeId(10L);

        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserLogin).thenReturn(Optional.empty());

            // Act + Assert
            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> seatHoldService.createSeatHold(req));
            assertEquals("Unauthenticated", ex.getMessage());
            verifyNoInteractions(userRepository, seatRepository, showTimeRepository, seatHoldRepository);
        }
    }

    @Test
    void createSeatHold_shouldThrow_whenShowTimeNotFound() {
        // Arrange
        String email = "test@gmail.com";

        ReqCreateSeatHoldDto req = new ReqCreateSeatHoldDto();
        req.setSeatIds(List.of(100L));
        req.setShowtimeId(10L);

        User user = new User();

        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserLogin).thenReturn(Optional.of(email));

            when(userRepository.findUserByEmail(email)).thenReturn(user);
            when(showTimeRepository.findById(10L)).thenReturn(Optional.empty());

            // Act + Assert
            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> seatHoldService.createSeatHold(req));
            assertEquals("ShowTime not found", ex.getMessage());

            verify(seatHoldRepository, never()).saveAll(anyList());
        }
    }

    @Test
    void createSeatHold_shouldThrow_whenSomeSeatNotFound() {
        // Arrange
        String email = "test@gmail.com";

        ReqCreateSeatHoldDto req = new ReqCreateSeatHoldDto();
        req.setSeatIds(List.of(100L, 101L));
        req.setShowtimeId(10L);

        User user = new User();
        Seat seat = new Seat();
        seat.setId(100L);
        
        ShowTime showTime = new ShowTime();
        showTime.setId(10L);

        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserLogin).thenReturn(Optional.of(email));

            when(userRepository.findUserByEmail(email)).thenReturn(user);
            when(showTimeRepository.findById(10L)).thenReturn(Optional.of(showTime));
            // Only return 1 seat when requesting 2
            when(seatRepository.lockSeats(List.of(100L, 101L))).thenReturn(List.of(seat));

            // Act + Assert
            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> seatHoldService.createSeatHold(req));
            assertEquals("Some seats not found", ex.getMessage());

            verify(seatHoldRepository, never()).saveAll(anyList());
        }
    }

    @Test
    void createSeatHold_shouldThrow_whenSeatAlreadyHeld() {
        // Arrange
        String email = "test@gmail.com";

        ReqCreateSeatHoldDto req = new ReqCreateSeatHoldDto();
        req.setSeatIds(List.of(100L));
        req.setShowtimeId(10L);

        User user = new User();
        Seat seat = new Seat();
        seat.setId(100L);
        
        ShowTime showTime = new ShowTime();
        showTime.setId(10L);

        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserLogin).thenReturn(Optional.of(email));

            when(userRepository.findUserByEmail(email)).thenReturn(user);
            when(showTimeRepository.findById(10L)).thenReturn(Optional.of(showTime));
            when(seatRepository.lockSeats(List.of(100L))).thenReturn(List.of(seat));
            when(seatHoldRepository.existsBySeatIdAndShowTimeId(100L, 10L)).thenReturn(true);

            // Act + Assert
            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> seatHoldService.createSeatHold(req));
            assertEquals("Seat 100 is already held for this showtime", ex.getMessage());

            verify(seatHoldRepository, never()).saveAll(anyList());
        }
    }

    @Test
    void getSeatHoldByUserId_shouldReturnListFromRepository() {
        // Arrange
        Long userId = 1L;
        List<SeatHold> expected = List.of(new SeatHold(), new SeatHold());

        when(seatHoldRepository.findByUserIdFetchFull(userId)).thenReturn(expected);

        // Act
        List<SeatHold> result = seatHoldService.getSeatHoldByUserId(userId);

        // Assert
        assertEquals(expected, result);
        verify(seatHoldRepository).findByUserIdFetchFull(userId);
    }
}
