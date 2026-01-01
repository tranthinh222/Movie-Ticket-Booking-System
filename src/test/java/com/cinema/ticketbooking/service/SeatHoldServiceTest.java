package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.Seat;
import com.cinema.ticketbooking.domain.SeatHold;
import com.cinema.ticketbooking.domain.ShowTime;
import com.cinema.ticketbooking.domain.User;
import com.cinema.ticketbooking.domain.request.ReqCreateSeatHoldDto;
import com.cinema.ticketbooking.repository.SeatHoldRepository;
import com.cinema.ticketbooking.repository.SeatRepository;
import com.cinema.ticketbooking.repository.ShowTimeRepository;
import com.cinema.ticketbooking.repository.UserRepository;
import com.cinema.ticketbooking.util.SecurityUtil;
import com.cinema.ticketbooking.util.constant.SeatStatusEnum;

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

    @InjectMocks private SeatHoldService seatHoldService;

    @Test
    void createSeatHold_shouldCreateSeatHold_whenAuthenticatedAndDataValid() {
        // Arrange
        String email = "test@gmail.com";

        ReqCreateSeatHoldDto req = new ReqCreateSeatHoldDto();
        req.setSeatId(100L);
        req.setShowtimeId(10L);

        User user = new User();
        user.setId(1L);

        Seat seat = new Seat();
        seat.setId(100L);
        seat.setStatus(SeatStatusEnum.AVAILABLE);

        ShowTime showTime = new ShowTime();
        showTime.setId(10L);

        Instant start = Instant.now();

        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserLogin).thenReturn(Optional.of(email));

            when(userRepository.findUserByEmail(email)).thenReturn(user);
            when(seatRepository.findById(100L)).thenReturn(Optional.of(seat));
            when(showTimeRepository.findById(10L)).thenReturn(Optional.of(showTime));

            // Act
            SeatHold result = seatHoldService.createSeatHold(req);

            // Assert
            assertNotNull(result);
            assertEquals(user, result.getUser());
            assertEquals(seat, result.getSeat());
            assertEquals(showTime, result.getShowTime());

            // Seat phải chuyển sang HOLD và được lưu
            assertEquals(SeatStatusEnum.HOLD, seat.getStatus());
            verify(seatRepository).save(seat);

            // SeatHold được save
            verify(seatHoldRepository).save(any(SeatHold.class));

            assertNotNull(result.getExpiresAt());
            Instant min = start.plus(9, ChronoUnit.MINUTES);
            Instant max = start.plus(11, ChronoUnit.MINUTES);
            assertTrue(result.getExpiresAt().isAfter(min));
            assertTrue(result.getExpiresAt().isBefore(max));
        }
    }

    @Test
    void createSeatHold_shouldThrowUnauthenticated_whenNoLogin() {
        // Arrange
        ReqCreateSeatHoldDto req = new ReqCreateSeatHoldDto();
        req.setSeatId(100L);
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
        req.setSeatId(100L);
        req.setShowtimeId(10L);

        User user = new User();
        Seat seat = new Seat();
        seat.setId(100L);
        seat.setStatus(SeatStatusEnum.AVAILABLE);

        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserLogin).thenReturn(Optional.of(email));

            when(userRepository.findUserByEmail(email)).thenReturn(user);
            when(seatRepository.findById(100L)).thenReturn(Optional.of(seat));
            when(showTimeRepository.findById(10L)).thenReturn(Optional.empty());

            // Act + Assert
            RuntimeException ex = assertThrows(RuntimeException.class,
                    () -> seatHoldService.createSeatHold(req));
            assertEquals("ShowTime not found", ex.getMessage());

            // Vì code set HOLD + save seat trước khi check showtime
            assertEquals(SeatStatusEnum.HOLD, seat.getStatus());
            verify(seatRepository).save(seat);
            verify(seatHoldRepository, never()).save(any());
        }
    }

    @Test
    void createSeatHold_shouldThrowNoSuchElement_whenSeatNotFound() {
        // Arrange
        String email = "test@gmail.com";

        ReqCreateSeatHoldDto req = new ReqCreateSeatHoldDto();
        req.setSeatId(100L);
        req.setShowtimeId(10L);

        User user = new User();

        try (MockedStatic<SecurityUtil> mocked = mockStatic(SecurityUtil.class)) {
            mocked.when(SecurityUtil::getCurrentUserLogin).thenReturn(Optional.of(email));

            when(userRepository.findUserByEmail(email)).thenReturn(user);
            when(seatRepository.findById(100L)).thenReturn(Optional.empty());

            // Act + Assert
            assertThrows(java.util.NoSuchElementException.class,
                    () -> seatHoldService.createSeatHold(req));

            verify(seatRepository, never()).save(any());
            verify(seatHoldRepository, never()).save(any());
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
