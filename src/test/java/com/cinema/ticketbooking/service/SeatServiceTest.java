package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.Auditorium;
import com.cinema.ticketbooking.domain.Seat;
import com.cinema.ticketbooking.domain.SeatVariant;
import com.cinema.ticketbooking.domain.request.ReqCreateSeatDto;
import com.cinema.ticketbooking.domain.request.ReqUpdateSeatDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.repository.AuditoriumRepository;
import com.cinema.ticketbooking.repository.SeatRepository;
import com.cinema.ticketbooking.repository.SeatVariantRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeatServiceTest {

    @Mock private SeatRepository seatRepository;
    @Mock private AuditoriumRepository auditoriumRepository;
    @Mock private SeatVariantRepository seatVariantRepository;

    @InjectMocks private SeatService seatService;

    // -----------------------
    // getAllSeats
    // -----------------------
    @Test
    void getAllSeats_shouldReturnPaginationMetaAndData() {
        // Arrange
        Specification<Seat> spec = null;  
        Pageable pageable = PageRequest.of(0, 10);  

        Seat s1 = new Seat();
        Seat s2 = new Seat();
        List<Seat> content = List.of(s1, s2);

        Page<Seat> page = new PageImpl<>(content, pageable, 25);  

        when(seatRepository.findAll(spec, pageable)).thenReturn(page);

        // Act
        ResultPaginationDto result = seatService.getAllSeats(spec, pageable);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getMeta());
        assertEquals(1, result.getMeta().getCurrentPage());
        assertEquals(3, result.getMeta().getTotalPages());
        assertEquals(10, result.getMeta().getPageSize());
        assertEquals(25, result.getMeta().getTotalItems());

        assertEquals(content, result.getData());
        verify(seatRepository).findAll(spec, pageable);
    }

    // -----------------------
    // findSeatById
    // -----------------------
    @Test
    void findSeatById_shouldReturnSeat_whenExists() {
        // Arrange
        Seat seat = new Seat();
        when(seatRepository.findById(1L)).thenReturn(Optional.of(seat));

        // Act
        Seat result = seatService.findSeatById(1L);

        // Assert
        assertSame(seat, result);
        verify(seatRepository).findById(1L);
    }

    @Test
    void findSeatById_shouldReturnNull_whenNotFound() {
        // Arrange
        when(seatRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Seat result = seatService.findSeatById(1L);

        // Assert
        assertNull(result);
        verify(seatRepository).findById(1L);
    }

    // -----------------------
    // deleteSeat
    // -----------------------
    @Test
    void deleteSeat_shouldCallRepositoryDeleteById() {
        // Act
        seatService.deleteSeat(5L);

        // Assert
        verify(seatRepository).deleteById(5L);
    }

    // -----------------------
    // createSeat
    // -----------------------
    @Test
    void createSeat_shouldCreateAndSaveSeat_whenAuditoriumAndVariantFound() {
        // Arrange
        ReqCreateSeatDto req = new ReqCreateSeatDto();
        req.setAuditoriumId(10L);
        req.setSeatVariantId(20L);
        req.setSeatRow("A");
        req.setNumber(1);

        Auditorium auditorium = new Auditorium();
        SeatVariant variant = new SeatVariant();

        when(auditoriumRepository.findById(10L)).thenReturn(Optional.of(auditorium));
        when(seatVariantRepository.findById(20L)).thenReturn(Optional.of(variant));

        ArgumentCaptor<Seat> seatCaptor = ArgumentCaptor.forClass(Seat.class);
        when(seatRepository.save(any(Seat.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Seat created = seatService.createSeat(req);

        // Assert
        verify(seatRepository).save(seatCaptor.capture());
        Seat savedSeat = seatCaptor.getValue();

        assertSame(auditorium, savedSeat.getAuditorium());
        assertSame(variant, savedSeat.getSeatVariant());
        assertEquals("A", savedSeat.getSeatRow());
        assertEquals(1, savedSeat.getNumber());

        // Note: Seat entity no longer has status field
        assertSame(created, savedSeat);
    }

    @Test
    void createSeat_shouldSaveSeatWithNullRefs_whenAuditoriumOrVariantNotFound() {
        // Arrange
        ReqCreateSeatDto req = new ReqCreateSeatDto();
        req.setAuditoriumId(10L);
        req.setSeatVariantId(20L);
        req.setSeatRow("B");
        req.setNumber(2);

        when(auditoriumRepository.findById(10L)).thenReturn(Optional.empty());
        when(seatVariantRepository.findById(20L)).thenReturn(Optional.empty());

        ArgumentCaptor<Seat> seatCaptor = ArgumentCaptor.forClass(Seat.class);
        when(seatRepository.save(any(Seat.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Seat created = seatService.createSeat(req);

        // Assert
        verify(seatRepository).save(seatCaptor.capture());
        Seat saved = seatCaptor.getValue();

        assertNull(saved.getAuditorium());
        assertNull(saved.getSeatVariant());
        assertEquals("B", saved.getSeatRow());
        assertEquals(2, saved.getNumber());

        assertSame(created, saved);
    }

    // -----------------------
    // updateSeat
    // -----------------------
    @Test
    void updateSeat_shouldReturnNull_whenSeatNotFound() {
        // Arrange
        ReqUpdateSeatDto req = new ReqUpdateSeatDto();
        req.setId(99L);

        when(seatRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Seat result = seatService.updateSeat(req);

        // Assert
        assertNull(result);
        verify(seatRepository, never()).save(any());
    }

    @Test
    void updateSeat_shouldUpdateAndSave_whenSeatExists() {
        // Arrange
        Seat existing = new Seat();
        existing.setSeatRow("A");
        existing.setNumber(1);

        when(seatRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(seatRepository.save(any(Seat.class))).thenAnswer(inv -> inv.getArgument(0));

        ReqUpdateSeatDto req = new ReqUpdateSeatDto();
        req.setId(1L);
        req.setSeatRow("C");
        req.setNumber(9);

        // Act
        Seat updated = seatService.updateSeat(req);

        // Assert
        assertNotNull(updated);
        assertSame(existing, updated);
        assertEquals("C", updated.getSeatRow());
        assertEquals(9, updated.getNumber());

        // Note: Seat entity no longer has status field
        verify(seatRepository).save(existing);
    }
}
