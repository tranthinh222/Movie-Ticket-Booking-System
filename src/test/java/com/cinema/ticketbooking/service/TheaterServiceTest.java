package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.Address;
import com.cinema.ticketbooking.domain.Auditorium;
import com.cinema.ticketbooking.domain.Theater;
import com.cinema.ticketbooking.domain.request.ReqCreateTheaterDto;
import com.cinema.ticketbooking.domain.request.ReqUpdateTheaterDto;
import com.cinema.ticketbooking.domain.response.ResAuditoriumDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.repository.AddressRepository;
import com.cinema.ticketbooking.repository.TheaterRepository;
import com.cinema.ticketbooking.util.error.BadRequestException;
import com.cinema.ticketbooking.util.error.NotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TheaterServiceTest {

    @Mock
    private TheaterRepository theaterRepository;
    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private TheaterService theaterService;

    // -----------------------
    // getAllTheaters
    // -----------------------
    @Test
    void getAllTheaters_shouldReturnMetaAndData_followingCurrentImplementation() {
        // Arrange
        Specification<Theater> spec = null;
        Pageable pageable = PageRequest.of(0, 10);

        List<Theater> content = List.of(new Theater(), new Theater());
        Page<Theater> page = new PageImpl<>(content, pageable, 25); // totalElements=25, totalPages=3

        when(theaterRepository.findAll(spec, pageable)).thenReturn(page);

        // Act
        ResultPaginationDto result = theaterService.getAllTheaters(spec, pageable);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getMeta());
        assertEquals(content, result.getData());

        // Correct pagination meta values
        assertEquals(1, result.getMeta().getCurrentPage()); // pageable.getPageNumber() + 1 = 0 + 1 = 1
        assertEquals(3, result.getMeta().getTotalPages()); // theaterPage.getTotalPages() = 3
        assertEquals(10, result.getMeta().getPageSize()); // pageable.getPageSize() = 10
        assertEquals(25, result.getMeta().getTotalItems()); // theaterPage.getTotalElements() = 25

        verify(theaterRepository).findAll(spec, pageable);
    }

    // -----------------------
    // createTheater
    // -----------------------
    @Test
    void createTheater_shouldSaveWithAddress_whenAddressIdProvidedAndFound() {
        // Arrange
        ReqCreateTheaterDto req = new ReqCreateTheaterDto();
        req.setName("CGV");
        req.setAddressId(1L);

        Address address = new Address();
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        Theater savedReturn = new Theater();
        when(theaterRepository.save(any(Theater.class))).thenReturn(savedReturn);

        ArgumentCaptor<Theater> captor = ArgumentCaptor.forClass(Theater.class);

        // Act
        Theater result = theaterService.createTheater(req);

        // Assert
        assertSame(savedReturn, result);

        verify(theaterRepository).save(captor.capture());
        Theater savedArg = captor.getValue();

        assertEquals("CGV", savedArg.getName());
        assertSame(address, savedArg.getAddress());
    }

    @Test
    void createTheater_shouldSaveWithNullAddress_whenAddressIdProvidedButNotFound() {
        // Arrange
        ReqCreateTheaterDto req = new ReqCreateTheaterDto();
        req.setName("Lotte");
        req.setAddressId(99L);

        when(addressRepository.findById(99L)).thenReturn(Optional.empty());
        when(theaterRepository.save(any(Theater.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<Theater> captor = ArgumentCaptor.forClass(Theater.class);

        // Act
        Theater result = theaterService.createTheater(req);

        // Assert
        verify(theaterRepository).save(captor.capture());
        Theater savedArg = captor.getValue();

        assertEquals("Lotte", savedArg.getName());
        assertNull(savedArg.getAddress());

        assertSame(result, savedArg);
    }

    @Test
    void createTheater_shouldSaveWithNullAddress_whenAddressIdIsNull() {
        // Arrange
        ReqCreateTheaterDto req = new ReqCreateTheaterDto();
        req.setName("BHD");
        req.setAddressId(null);

        when(theaterRepository.save(any(Theater.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<Theater> captor = ArgumentCaptor.forClass(Theater.class);

        // Act
        Theater result = theaterService.createTheater(req);

        // Assert
        verify(theaterRepository).save(captor.capture());
        Theater savedArg = captor.getValue();

        assertEquals("BHD", savedArg.getName());
        assertNull(savedArg.getAddress());

        assertSame(result, savedArg);
        verifyNoInteractions(addressRepository);
    }

    // -----------------------
    // findTheaterById / deleteTheater
    // -----------------------
    @Test
    void findTheaterById_shouldReturnTheaterOrNull() {
        // Arrange
        Theater t = new Theater();
        when(theaterRepository.findById(1L)).thenReturn(Optional.of(t));
        when(theaterRepository.findById(2L)).thenReturn(Optional.empty());

        // Act + Assert
        assertSame(t, theaterService.findTheaterById(1L));
        assertNull(theaterService.findTheaterById(2L));
    }

    @Test
    void deleteTheater_shouldCallRepositoryDeleteById() {
        theaterService.deleteTheater(5L);
        verify(theaterRepository).deleteById(5L);
    }

    // -----------------------
    // updateTheater
    // -----------------------
    @Test
    void updateTheater_shouldThrowBadRequest_whenNoUpdatableField() {
        // Arrange
        ReqUpdateTheaterDto req = new ReqUpdateTheaterDto();
        req.setId(1L);
        req.setName("   "); // blank => hasUpdatableField = false

        // Act + Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> theaterService.updateTheater(req));
        assertEquals("No data provided for update", ex.getMessage());

        verifyNoInteractions(theaterRepository);
    }

    @Test
    void updateTheater_shouldReturnNull_whenTheaterNotFound() {
        // Arrange
        ReqUpdateTheaterDto req = new ReqUpdateTheaterDto();
        req.setId(99L);
        req.setName("New Name");

        when(theaterRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Theater result = theaterService.updateTheater(req);

        // Assert
        assertNull(result);
        verify(theaterRepository, never()).save(any());
    }

    @Test
    void updateTheater_shouldUpdateNameAndSave_whenValid() {
        // Arrange
        Theater existing = new Theater();
        existing.setName("Old");

        when(theaterRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(theaterRepository.save(any(Theater.class))).thenAnswer(inv -> inv.getArgument(0));

        ReqUpdateTheaterDto req = new ReqUpdateTheaterDto();
        req.setId(1L);
        req.setName("New Name");

        // Act
        Theater result = theaterService.updateTheater(req);

        // Assert
        assertNotNull(result);
        assertSame(existing, result);
        assertEquals("New Name", existing.getName());
        verify(theaterRepository).save(existing);
    }

    // -----------------------
    // getAuditoriumsByTheaterId
    // -----------------------
    @Test
    void getAuditoriumsByTheaterId_shouldThrowNotFound_whenTheaterMissing() {
        // Arrange
        when(theaterRepository.findById(1L)).thenReturn(Optional.empty());

        // Act + Assert
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> theaterService.getAuditoriumsByTheaterId(1L));
        assertEquals("Theater not found", ex.getMessage());
    }

    @Test
    void getAuditoriumsByTheaterId_shouldMapAuditoriumsToDtoList() {
        // Arrange
        Auditorium a1 = new Auditorium();
        a1.setId(1L);
        a1.setNumber(1L);
        a1.setTotalSeats(100L);
        a1.setCreatedAt(Instant.now());
        a1.setUpdatedAt(Instant.now());
        a1.setCreatedBy("u1");
        a1.setUpdatedBy("u2");

        Auditorium a2 = new Auditorium();
        a2.setId(2L);
        a2.setNumber(2L);
        a2.setTotalSeats(120L);
        a2.setCreatedAt(Instant.now());
        a2.setUpdatedAt(Instant.now());
        a2.setCreatedBy("u3");
        a2.setUpdatedBy("u4");

        Theater theater = new Theater();
        // cần list không null để stream được
        theater.setAuditoriums(new ArrayList<>(List.of(a1, a2)));

        when(theaterRepository.findById(10L)).thenReturn(Optional.of(theater));

        // Act
        List<ResAuditoriumDto> dtos = theaterService.getAuditoriumsByTheaterId(10L);

        // Assert
        assertNotNull(dtos);
        assertEquals(2, dtos.size());

        ResAuditoriumDto d1 = dtos.get(0);
        assertEquals(1L, d1.getId());
        assertEquals(1, d1.getNumber());
        assertEquals(100, d1.getTotalSeats());
        assertEquals(a1.getCreatedAt(), d1.getCreatedAt());
        assertEquals(a1.getUpdatedAt(), d1.getUpdatedAt());
        assertEquals("u1", d1.getCreatedBy());
        assertEquals("u2", d1.getUpdatedBy());

        ResAuditoriumDto d2 = dtos.get(1);
        assertEquals(2L, d2.getId());
        assertEquals(2, d2.getNumber());
        assertEquals(120, d2.getTotalSeats());
        assertEquals(a2.getCreatedAt(), d2.getCreatedAt());
        assertEquals(a2.getUpdatedAt(), d2.getUpdatedAt());
        assertEquals("u3", d2.getCreatedBy());
        assertEquals("u4", d2.getUpdatedBy());
    }
}
