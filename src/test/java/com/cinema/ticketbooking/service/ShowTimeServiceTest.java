package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.Auditorium;
import com.cinema.ticketbooking.domain.Film;
import com.cinema.ticketbooking.domain.ShowTime;
import com.cinema.ticketbooking.domain.request.ReqCreateShowTimeDto;
import com.cinema.ticketbooking.domain.request.ReqUpdateShowTimeDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.repository.AuditoriumRepository;
import com.cinema.ticketbooking.repository.BookingItemRepository;
import com.cinema.ticketbooking.repository.FilmRepository;
import com.cinema.ticketbooking.repository.ShowTimeRepository;

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
class ShowTimeServiceTest {

    @Mock
    private ShowTimeRepository showTimeRepository;
    @Mock
    private FilmRepository filmRepository;
    @Mock
    private AuditoriumRepository auditoriumRepository;
    @Mock
    private BookingItemRepository bookingItemRepository;

    @InjectMocks
    private ShowTimeService showTimeService;

    // -----------------------
    // getAllShowTimes
    // -----------------------
    @Test
    void getAllShowTimes_shouldReturnPaginationMetaAndData() {
        // Arrange
        Specification<ShowTime> spec = null;
        Pageable pageable = PageRequest.of(0, 10); // currentPage=1

        List<ShowTime> content = List.of(new ShowTime(), new ShowTime(), new ShowTime());
        Page<ShowTime> page = new PageImpl<>(content, pageable, 21); // totalPages=3

        when(showTimeRepository.findAll(spec, pageable)).thenReturn(page);

        // Act
        ResultPaginationDto result = showTimeService.getAllShowTimes(spec, pageable);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getMeta());
        assertEquals(1, result.getMeta().getCurrentPage());
        assertEquals(3, result.getMeta().getTotalPages());
        assertEquals(10, result.getMeta().getPageSize());
        assertEquals(21, result.getMeta().getTotalItems());
        assertEquals(content, result.getData());

        verify(showTimeRepository).findAll(spec, pageable);
    }

    // -----------------------
    // findShowTimeById
    // -----------------------
    @Test
    void findShowTimeById_shouldReturnShowTime_whenExists() {
        // Arrange
        ShowTime st = new ShowTime();
        when(showTimeRepository.findById(1L)).thenReturn(Optional.of(st));

        // Act
        ShowTime result = showTimeService.findShowTimeById(1L);

        // Assert
        assertSame(st, result);
        verify(showTimeRepository).findById(1L);
    }

    @Test
    void findShowTimeById_shouldReturnNull_whenNotFound() {
        // Arrange
        when(showTimeRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        ShowTime result = showTimeService.findShowTimeById(1L);

        // Assert
        assertNull(result);
        verify(showTimeRepository).findById(1L);
    }

    // -----------------------
    // createShowTime
    // -----------------------
    @Test
    void createShowTime_shouldCreateAndSave_whenAuditoriumAndFilmFound() {
        // Arrange
        ReqCreateShowTimeDto req = new ReqCreateShowTimeDto();
        req.setAuditoriumId(10L);
        req.setFilmId(20L);

        // Các field còn lại: date/start/end
        // Bạn chỉ cần set giá trị hợp lệ đúng kiểu của bạn (LocalDate, LocalTime, ...)
        // Ví dụ nếu date là LocalDate:
        // req.setDate(LocalDate.of(2025, 1, 1));
        // req.setStartTime(LocalTime.of(18, 0));
        // req.setEndTime(LocalTime.of(20, 0));

        Auditorium auditorium = new Auditorium();
        Film film = new Film();

        when(auditoriumRepository.findById(10L)).thenReturn(Optional.of(auditorium));
        when(filmRepository.findById(20L)).thenReturn(Optional.of(film));
        when(showTimeRepository.save(any(ShowTime.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<ShowTime> captor = ArgumentCaptor.forClass(ShowTime.class);

        // Act
        ShowTime created = showTimeService.createShowTime(req);

        // Assert
        verify(showTimeRepository).save(captor.capture());
        ShowTime saved = captor.getValue();

        assertSame(auditorium, saved.getAuditorium());
        assertSame(film, saved.getFilm());

        assertEquals(req.getDate(), saved.getDate());
        assertEquals(req.getStartTime(), saved.getStartTime());
        assertEquals(req.getEndTime(), saved.getEndTime());

        assertSame(created, saved);
    }

    @Test
    void createShowTime_shouldSaveWithNullRefs_whenAuditoriumOrFilmNotFound() {
        // Arrange
        ReqCreateShowTimeDto req = new ReqCreateShowTimeDto();
        req.setAuditoriumId(10L);
        req.setFilmId(20L);
        // set date/start/end nếu cần như trên

        when(auditoriumRepository.findById(10L)).thenReturn(Optional.empty());
        when(filmRepository.findById(20L)).thenReturn(Optional.empty());
        when(showTimeRepository.save(any(ShowTime.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<ShowTime> captor = ArgumentCaptor.forClass(ShowTime.class);

        // Act
        ShowTime created = showTimeService.createShowTime(req);

        // Assert
        verify(showTimeRepository).save(captor.capture());
        ShowTime saved = captor.getValue();

        assertNull(saved.getAuditorium());
        assertNull(saved.getFilm());

        assertEquals(req.getDate(), saved.getDate());
        assertEquals(req.getStartTime(), saved.getStartTime());
        assertEquals(req.getEndTime(), saved.getEndTime());

        assertSame(created, saved);
    }

    // -----------------------
    // deleteShowTime
    // -----------------------
    @Test
    void deleteShowTime_shouldCallRepositoryDeleteById() {
        // Act
        showTimeService.deleteShowTime(5L);

        // Assert
        verify(showTimeRepository).deleteById(5L);
    }

    // -----------------------
    // updateShowTime
    // -----------------------
    @Test
    void updateShowTime_shouldReturnNull_whenShowTimeNotFound() {
        // Arrange
        ReqUpdateShowTimeDto req = new ReqUpdateShowTimeDto();
        req.setId(99L);

        when(showTimeRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        ShowTime result = showTimeService.updateShowTime(req);

        // Assert
        assertNull(result);
        verify(showTimeRepository, never()).save(any());
    }

    @Test
    void updateShowTime_shouldUpdateAndSave_whenShowTimeExists() {
        // Arrange
        ShowTime existing = new ShowTime();
        when(showTimeRepository.findById(1L)).thenReturn(Optional.of(existing));

        ShowTime savedReturn = new ShowTime();
        when(showTimeRepository.save(any(ShowTime.class))).thenReturn(savedReturn);

        ReqUpdateShowTimeDto req = new ReqUpdateShowTimeDto();
        req.setId(1L);
        // set date/start/end hợp lệ
        // req.setDate(...)
        // req.setStartTime(...)
        // req.setEndTime(...)

        // Act
        ShowTime result = showTimeService.updateShowTime(req);

        // Assert
        assertSame(savedReturn, result);

        // existing bị set theo req trước khi save
        assertEquals(req.getDate(), existing.getDate());
        assertEquals(req.getStartTime(), existing.getStartTime());
        assertEquals(req.getEndTime(), existing.getEndTime());

        verify(showTimeRepository).save(existing);
    }

    // -----------------------
    // hasBookings
    // -----------------------
    @Test
    void hasBookings_shouldReturnTrue_whenBookingsExist() {
        // Arrange
        when(bookingItemRepository.existsByShowTimeId(1L)).thenReturn(true);

        // Act
        boolean result = showTimeService.hasBookings(1L);

        // Assert
        assertTrue(result);
        verify(bookingItemRepository).existsByShowTimeId(1L);
    }

    @Test
    void hasBookings_shouldReturnFalse_whenNoBookingsExist() {
        // Arrange
        when(bookingItemRepository.existsByShowTimeId(2L)).thenReturn(false);

        // Act
        boolean result = showTimeService.hasBookings(2L);

        // Assert
        assertFalse(result);
        verify(bookingItemRepository).existsByShowTimeId(2L);
    }
}
