package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.*;
import com.cinema.ticketbooking.domain.request.ReqCreateAuditoriumDto;
import com.cinema.ticketbooking.domain.request.ReqUpdateAuditoriumDto;
import com.cinema.ticketbooking.domain.response.ResSeatDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.repository.AuditoriumRepository;
import com.cinema.ticketbooking.repository.TheaterRepository;
import com.cinema.ticketbooking.util.constant.SeatTypeEnum;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditoriumServiceTest {

    @Mock
    private AuditoriumRepository auditoriumRepository;

    @Mock
    private TheaterRepository theaterRepository;

    @Mock
    private SeatService seatService;

    @InjectMocks
    private AuditoriumService auditoriumService;

    @Test
    void getAllAuditoriums_shouldReturnPaginationResult_whenCalled() {
        // Arrange
        Page<Auditorium> page = new PageImpl<>(
                List.of(new Auditorium()),
                PageRequest.of(0, 10),
                1
        );

        when(auditoriumRepository.findAll(
                ArgumentMatchers.<Specification<Auditorium>>any(),
                any(Pageable.class)
        )).thenReturn(page);

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        ResultPaginationDto result =
                auditoriumService.getAllAuditoriums(null, pageable);

        // Assert
        assertEquals(1, result.getMeta().getTotalItems());

        @SuppressWarnings("unchecked")
        List<Auditorium> data = (List<Auditorium>) result.getData();
        assertEquals(1, data.size());
    }

    @Test
    void createAuditorium_shouldCreateSeatsAndReturnAuditorium_whenTheaterExists() {
        // Arrange
        ReqCreateAuditoriumDto req = new ReqCreateAuditoriumDto();
        req.setNumber(1L);
        req.setTheaterId(10L);

        Theater theater = new Theater();
        when(theaterRepository.findById(10L))
                .thenReturn(Optional.of(theater));

        // Mock auditoriumRepository.save to return auditorium with ID
        Auditorium savedAuditorium = new Auditorium();
        savedAuditorium.setNumber(1L);
        savedAuditorium.setTheater(theater);
        when(auditoriumRepository.save(any(Auditorium.class))).thenReturn(savedAuditorium);

        // Mock seatService to return list of seats
        List<Seat> createdSeats = List.of(new Seat(), new Seat(), new Seat());
        when(seatService.createDefaultSeatsForAuditorium(any(Auditorium.class))).thenReturn(createdSeats);

        // Act
        Auditorium result = auditoriumService.createAuditorium(req);

        // Assert
        assertEquals(1L, result.getNumber());
        assertEquals(theater, result.getTheater());
        // totalSeats is set based on created seats (second save)
        verify(auditoriumRepository, times(2)).save(any(Auditorium.class));
        verify(seatService).createDefaultSeatsForAuditorium(any(Auditorium.class));
    }

    @Test
    void createAuditorium_shouldThrowException_whenTheaterNotFound() {
        // Arrange
        ReqCreateAuditoriumDto req = new ReqCreateAuditoriumDto();
        req.setNumber(2L);
        req.setTheaterId(99L);

        when(theaterRepository.findById(99L))
                .thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> auditoriumService.createAuditorium(req));
        assertEquals("Theater not found", ex.getMessage());
        verify(auditoriumRepository, never()).save(any());
    }

    @Test
    void getAuditoriumById_shouldReturnAuditorium_whenFound() {
        // Arrange
        Auditorium auditorium = new Auditorium();
        when(auditoriumRepository.findById(1L))
                .thenReturn(Optional.of(auditorium));

        // Act
        Auditorium result = auditoriumService.getAuditoriumById(1L);

        // Assert
        assertEquals(auditorium, result);
    }

    @Test
    void getAuditoriumById_shouldReturnNull_whenNotFound() {
        // Arrange
        when(auditoriumRepository.findById(1L))
                .thenReturn(Optional.empty());

        // Act
        Auditorium result = auditoriumService.getAuditoriumById(1L);

        // Assert
        assertNull(result);
    }

    @Test
    void deleteAuditorium_shouldCallRepository_whenCalled() {
        // Act
        auditoriumService.deleteAuditorium(1L);

        // Assert
        verify(auditoriumRepository).deleteById(1L);
    }

    @Test
    void updateAuditorium_shouldSaveAndReturnAuditorium_whenCalled() {
        // Arrange
        ReqUpdateAuditoriumDto req = new ReqUpdateAuditoriumDto();
        req.setNumber(3L);           
        req.setTotalSeat(120L);

        when(auditoriumRepository.save(any(Auditorium.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Auditorium result = auditoriumService.updateAuditorium(req);

        // Assert
        assertEquals(3L, result.getNumber());
        assertEquals(120L, result.getTotalSeats());
        verify(auditoriumRepository).save(any(Auditorium.class));
    }

    @Test
    void convertToSeatDto_shouldMapAllFields_whenSeatHasVariant() {
        // Arrange
        SeatVariant variant = new SeatVariant();
        variant.setSeatType(SeatTypeEnum.VIP);  

        Seat seat = new Seat();
        seat.setId(1L);
        seat.setSeatRow("A");
        seat.setNumber(10);                 
        seat.setSeatVariant(variant);

        // Act
        ResSeatDto dto = auditoriumService.convertToSeatDto(seat);

        // Assert
        assertEquals("VIP", dto.getSeatVariantName());
        assertEquals(1L, dto.getId());
        assertEquals("A", dto.getSeatRow());
        assertEquals(10, dto.getNumber());
    }

    @Test
    void convertToSeatDto_shouldSetNullVariantName_whenVariantIsNull() {
        // Arrange
        Seat seat = new Seat();
        seat.setId(2L);
        seat.setSeatRow("B");
        seat.setNumber(5);

        // Act
        ResSeatDto dto = auditoriumService.convertToSeatDto(seat);

        // Assert
        assertNull(dto.getSeatVariantName());
        assertEquals(2L, dto.getId());
    }
}
