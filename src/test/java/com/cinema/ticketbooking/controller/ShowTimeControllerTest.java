package com.cinema.ticketbooking.controller;

import com.cinema.ticketbooking.domain.Auditorium;
import com.cinema.ticketbooking.domain.Film;
import com.cinema.ticketbooking.domain.ShowTime;
import com.cinema.ticketbooking.domain.request.ReqCreateShowTimeDto;
import com.cinema.ticketbooking.domain.request.ReqUpdateShowTimeDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.service.AuditoriumService;
import com.cinema.ticketbooking.service.FilmService;
import com.cinema.ticketbooking.service.ShowTimeService;
import com.cinema.ticketbooking.util.error.BadRequestException;
import com.cinema.ticketbooking.util.error.IdInvalidException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShowTimeControllerTest {

    @Mock
    private ShowTimeService showTimeService;
    @Mock
    private FilmService filmService;
    @Mock
    private AuditoriumService auditoriumService;

    @InjectMocks
    private ShowTimeController showTimeController;

    // -----------------------
    // GET /showtimes
    // -----------------------
    @Test
    void getAllShowTimes_shouldReturnOkAndBody() {
        // Arrange
        Specification<ShowTime> spec = null;
        Pageable pageable = PageRequest.of(0, 10);

        ResultPaginationDto dto = new ResultPaginationDto();
        when(showTimeService.getAllShowTimes(spec, pageable)).thenReturn(dto);

        // Act
        ResponseEntity<ResultPaginationDto> res = showTimeController.getAllShowTimes(spec, pageable);

        // Assert
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertSame(dto, res.getBody());
        verify(showTimeService).getAllShowTimes(spec, pageable);
    }

    // -----------------------
    // GET /showtimes/{id}
    // -----------------------
    @Test
    void getShowTimeById_shouldReturnOk_whenFound() {
        // Arrange
        ShowTime st = new ShowTime();
        when(showTimeService.findShowTimeById(1L)).thenReturn(st);

        // Act
        ResponseEntity<ShowTime> res = showTimeController.getShowTimeById(1L);

        // Assert
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertSame(st, res.getBody());
        verify(showTimeService).findShowTimeById(1L);
    }

    @Test
    void getShowTimeById_shouldThrowIdInvalidException_whenNotFound() {
        // Arrange
        when(showTimeService.findShowTimeById(99L)).thenReturn(null);

        // Act + Assert
        IdInvalidException ex = assertThrows(IdInvalidException.class,
                () -> showTimeController.getShowTimeById(99L));
        assertEquals("ShowTime with id 99 not found", ex.getMessage());
    }

    // -----------------------
    // POST /showtimes
    // -----------------------
    @Test
    void create_shouldThrow_whenFilmNotFound() {
        // Arrange
        ReqCreateShowTimeDto req = new ReqCreateShowTimeDto();
        req.setFilmId(10L);
        req.setAuditoriumId(20L);

        when(filmService.getFilmById(10L)).thenReturn(null);

        // Act + Assert
        IdInvalidException ex = assertThrows(IdInvalidException.class,
                () -> showTimeController.create(req));
        assertEquals("Film with id 10 not found", ex.getMessage());

        verify(showTimeService, never()).createShowTime(any());
    }

    @Test
    void create_shouldThrow_whenAuditoriumNotFound() {
        // Arrange
        ReqCreateShowTimeDto req = new ReqCreateShowTimeDto();
        req.setFilmId(10L);
        req.setAuditoriumId(20L);

        when(filmService.getFilmById(10L)).thenReturn(new Film());
        when(auditoriumService.getAuditoriumById(20L)).thenReturn(null);

        // Act + Assert
        IdInvalidException ex = assertThrows(IdInvalidException.class,
                () -> showTimeController.create(req));
        assertEquals("Auditorium with id 20 not found", ex.getMessage());

        verify(showTimeService, never()).createShowTime(any());
    }

    @Test
    void create_shouldReturnCreated_whenValid() {
        // Arrange
        ReqCreateShowTimeDto req = new ReqCreateShowTimeDto();
        req.setFilmId(10L);
        req.setAuditoriumId(20L);

        when(filmService.getFilmById(10L)).thenReturn(new Film());
        when(auditoriumService.getAuditoriumById(20L)).thenReturn(new Auditorium());

        ShowTime created = new ShowTime();
        when(showTimeService.createShowTime(req)).thenReturn(created);

        // Act
        ResponseEntity<ShowTime> res = showTimeController.create(req);

        // Assert
        assertEquals(HttpStatus.CREATED, res.getStatusCode());
        assertSame(created, res.getBody());
        verify(showTimeService).createShowTime(req);
    }

    // -----------------------
    // DELETE /showtimes/{id}
    // -----------------------
    @Test
    void deleteShowTime_shouldReturnOk_whenFound() {
        // Arrange
        ShowTime existingShowTime = new ShowTime();
        existingShowTime.setId(5L);
        when(showTimeService.findShowTimeById(5L)).thenReturn(existingShowTime);
        when(showTimeService.hasBookings(5L)).thenReturn(false);

        // Act
        ResponseEntity<Void> res = showTimeController.deleteShowTime(5L);

        // Assert
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertNull(res.getBody());
        verify(showTimeService).findShowTimeById(5L);
        verify(showTimeService).hasBookings(5L);
        verify(showTimeService).deleteShowTime(5L);
    }

    @Test
    void deleteShowTime_shouldThrowIdInvalidException_whenNotFound() {
        // Arrange
        when(showTimeService.findShowTimeById(5L)).thenReturn(null);

        // Act + Assert
        IdInvalidException ex = assertThrows(IdInvalidException.class,
                () -> showTimeController.deleteShowTime(5L));
        assertEquals("ShowTime with id 5 not found", ex.getMessage());

        verify(showTimeService, never()).deleteShowTime(anyLong());
    }

    // -----------------------
    // PUT /showtimes
    // -----------------------
    @Test
    void updateShowTime_shouldReturnOk_whenUpdated() {
        // Arrange
        ReqUpdateShowTimeDto req = new ReqUpdateShowTimeDto();
        req.setId(1L);

        ShowTime existingShowTime = new ShowTime();
        existingShowTime.setId(1L);

        ShowTime updated = new ShowTime();
        updated.setId(1L);

        when(showTimeService.findShowTimeById(1L)).thenReturn(existingShowTime);
        when(showTimeService.hasBookings(1L)).thenReturn(false);
        when(showTimeService.updateShowTime(req)).thenReturn(updated);

        // Act
        ResponseEntity<ShowTime> res = showTimeController.updateShowTime(req);

        // Assert
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertSame(updated, res.getBody());
        verify(showTimeService).findShowTimeById(1L);
        verify(showTimeService).hasBookings(1L);
        verify(showTimeService).updateShowTime(req);
    }

    @Test
    void updateShowTime_shouldThrowIdInvalidException_whenNotFound() {
        // Arrange
        ReqUpdateShowTimeDto req = new ReqUpdateShowTimeDto();
        req.setId(99L);

        when(showTimeService.findShowTimeById(99L)).thenReturn(null);

        // Act & Assert
        IdInvalidException ex = assertThrows(IdInvalidException.class,
                () -> showTimeController.updateShowTime(req));
        assertEquals("ShowTime with id 99 does not exist", ex.getMessage());

        verify(showTimeService).findShowTimeById(99L);
        verify(showTimeService, never()).updateShowTime(any());
    }

    @Test
    void updateShowTime_shouldThrowBadRequest_whenHasBookings() {
        // Arrange
        ReqUpdateShowTimeDto req = new ReqUpdateShowTimeDto();
        req.setId(1L);

        ShowTime existingShowTime = new ShowTime();
        existingShowTime.setId(1L);

        when(showTimeService.findShowTimeById(1L)).thenReturn(existingShowTime);
        when(showTimeService.hasBookings(1L)).thenReturn(true);

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> showTimeController.updateShowTime(req));
        assertEquals("Cannot update showtime with id 1 because it has existing bookings", ex.getMessage());

        verify(showTimeService).findShowTimeById(1L);
        verify(showTimeService).hasBookings(1L);
        verify(showTimeService, never()).updateShowTime(any());
    }

    @Test
    void deleteShowTime_shouldThrowBadRequest_whenHasBookings() {
        // Arrange
        ShowTime existingShowTime = new ShowTime();
        existingShowTime.setId(1L);

        when(showTimeService.findShowTimeById(1L)).thenReturn(existingShowTime);
        when(showTimeService.hasBookings(1L)).thenReturn(true);

        // Act & Assert
        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> showTimeController.deleteShowTime(1L));
        assertEquals("Cannot delete showtime with id 1 because it has existing bookings", ex.getMessage());

        verify(showTimeService).findShowTimeById(1L);
        verify(showTimeService).hasBookings(1L);
        verify(showTimeService, never()).deleteShowTime(anyLong());
    }
}
