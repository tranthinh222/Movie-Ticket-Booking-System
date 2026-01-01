package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.SeatVariant;
import com.cinema.ticketbooking.domain.request.ReqCreateSeatVariantDto;
import com.cinema.ticketbooking.domain.request.ReqUpdateSeatVariantDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.repository.SeatVariantRepository;
import com.cinema.ticketbooking.util.constant.SeatTypeEnum;

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
class SeatVariantServiceTest {

    @Mock
    private SeatVariantRepository seatVariantRepository;

    @InjectMocks
    private SeatVariantService seatVariantService;

    // -----------------------
    // getAllSeatVariants
    // -----------------------
    @Test
    void getAllSeatVariants_shouldReturnPaginationMetaAndData() {
        // Arrange
        Specification<SeatVariant> spec = null;
        Pageable pageable = PageRequest.of(1, 5); // page=1 => currentPage=2

        List<SeatVariant> content = List.of(new SeatVariant(), new SeatVariant());
        Page<SeatVariant> page = new PageImpl<>(content, pageable, 12); // totalItems=12 => totalPages=3

        when(seatVariantRepository.findAll(spec, pageable)).thenReturn(page);

        // Act
        ResultPaginationDto result = seatVariantService.getAllSeatVariants(spec, pageable);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getMeta());
        assertEquals(2, result.getMeta().getCurrentPage());
        assertEquals(3, result.getMeta().getTotalPages());
        assertEquals(5, result.getMeta().getPageSize());
        assertEquals(12, result.getMeta().getTotalItems());
        assertEquals(content, result.getData());

        verify(seatVariantRepository).findAll(spec, pageable);
    }

    // -----------------------
    // findSeatVariantById
    // -----------------------
    @Test
    void findSeatVariantById_shouldReturnVariant_whenExists() {
        // Arrange
        SeatVariant v = new SeatVariant();
        when(seatVariantRepository.findById(1L)).thenReturn(Optional.of(v));

        // Act
        SeatVariant result = seatVariantService.findSeatVariantById(1L);

        // Assert
        assertSame(v, result);
        verify(seatVariantRepository).findById(1L);
    }

    @Test
    void findSeatVariantById_shouldReturnNull_whenNotFound() {
        // Arrange
        when(seatVariantRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        SeatVariant result = seatVariantService.findSeatVariantById(1L);

        // Assert
        assertNull(result);
        verify(seatVariantRepository).findById(1L);
    }

    // -----------------------
    // deleteSeatVariant
    // -----------------------
    @Test
    void deleteSeatVariant_shouldCallRepositoryDeleteById() {
        // Act
        seatVariantService.deleteSeatVariant(10L);

        // Assert
        verify(seatVariantRepository).deleteById(10L);
    }

    // -----------------------
    // createSeatVariant
    // -----------------------
    @Test
    void createSeatVariant_shouldCreateAndSaveVariant() {
        // Arrange
        // Dùng enum values() để không phụ thuộc tên cụ thể (VIP/NORMAL/...)
        SeatTypeEnum type = SeatTypeEnum.values()[0];

        ReqCreateSeatVariantDto req = new ReqCreateSeatVariantDto();
        req.setBasePrice(100000.0);
        req.setSeatType(type);
        req.setBonus(20000.0);

        ArgumentCaptor<SeatVariant> captor = ArgumentCaptor.forClass(SeatVariant.class);
        when(seatVariantRepository.save(any(SeatVariant.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        SeatVariant created = seatVariantService.createSeatVariant(req);

        // Assert
        verify(seatVariantRepository).save(captor.capture());
        SeatVariant saved = captor.getValue();

        assertEquals(100000.0, saved.getBasePrice());
        assertEquals(type, saved.getSeatType());
        assertEquals(20000.0, saved.getBonus());

        // service trả đúng object đã tạo
        assertSame(created, saved);
    }

    // -----------------------
    // updateSeatVariant
    // -----------------------
    @Test
    void updateSeatVariant_shouldReturnNull_whenVariantNotFound() {
        // Arrange
        ReqUpdateSeatVariantDto req = new ReqUpdateSeatVariantDto();
        req.setId(99L);
        req.setSeatType(SeatTypeEnum.values()[0]);
        req.setBonus(0.0);

        when(seatVariantRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        SeatVariant result = seatVariantService.updateSeatVariant(req);

        // Assert
        assertNull(result);
        verify(seatVariantRepository, never()).save(any());
    }

    @Test
    void updateSeatVariant_shouldUpdateAndSave_whenVariantExists() {
        // Arrange
        SeatVariant existing = new SeatVariant();
        existing.setBasePrice(123456.0);                  
        existing.setSeatType(SeatTypeEnum.values()[0]);
        existing.setBonus(10000.0);

        when(seatVariantRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(seatVariantRepository.save(any(SeatVariant.class))).thenAnswer(inv -> inv.getArgument(0));

        SeatTypeEnum newType = SeatTypeEnum.values().length > 1 ? SeatTypeEnum.values()[1] : SeatTypeEnum.values()[0];

        ReqUpdateSeatVariantDto req = new ReqUpdateSeatVariantDto();
        req.setId(1L);
        req.setSeatType(newType);
        req.setBonus(30000.0);

        // Act
        SeatVariant updated = seatVariantService.updateSeatVariant(req);

        // Assert
        assertNotNull(updated);
        assertSame(existing, updated);

        assertEquals(newType, updated.getSeatType());
        assertEquals(30000.0, updated.getBonus());

        // basePrice giữ nguyên (vì service không set)
        assertEquals(123456.0, updated.getBasePrice());

        verify(seatVariantRepository).save(existing);
    }
}
