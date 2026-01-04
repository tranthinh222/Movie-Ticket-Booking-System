package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.Booking;
import com.cinema.ticketbooking.domain.Payment;
import com.cinema.ticketbooking.domain.User;
import com.cinema.ticketbooking.domain.response.ResCreateBookingDto;
import com.cinema.ticketbooking.repository.BookingRepository;
import com.cinema.ticketbooking.util.constant.BookingStatusEnum;
import com.cinema.ticketbooking.util.constant.PaymentMethodEnum;
import com.cinema.ticketbooking.util.constant.PaymentStatusEnum;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
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

    @Mock
    private PaymentService paymentService;

    @Mock
    private VNPayService vnPayService;

    @InjectMocks
    private BookingService bookingService;

    @Test
    void createBooking_shouldCreateBookingWithPayment_whenValidRequest_Cash() {
        // Arrange
        Long userId = 1L;
        PaymentMethodEnum paymentMethod = PaymentMethodEnum.CASH;
        String ipAddress = "127.0.0.1";

        User user = new User();
        user.setId(userId);
        user.setUsername("TestUser");

        Booking savedBooking = new Booking();
        savedBooking.setId(10L);
        savedBooking.setStatus(BookingStatusEnum.PENDING);
        savedBooking.setUser(user);

        Payment payment = new Payment();
        payment.setId(100L);

        when(userService.getUserById(userId)).thenReturn(user);
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
        when(bookingItemService.createListItem(userId, savedBooking)).thenReturn(200000.0);
        when(paymentService.createPayment(any(Booking.class), eq(paymentMethod))).thenReturn(payment);

        // Act
        ResCreateBookingDto result = bookingService.createBooking(userId, paymentMethod, ipAddress);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals("TestUser", result.getUsername());
        assertEquals(200000.0, result.getPrice());
        assertEquals(100L, result.getPaymentId());
        assertNull(result.getPaymentUrl()); // CASH không có payment URL

        verify(userService).getUserById(userId);
        verify(bookingRepository, times(2)).save(any(Booking.class));
        verify(bookingItemService).createListItem(userId, savedBooking);
        verify(paymentService).createPayment(any(Booking.class), eq(paymentMethod));
    }

    @Test
    void createBooking_shouldCreateBookingWithVNPayUrl_whenValidRequest_VNPay() throws UnsupportedEncodingException {
        // Arrange
        Long userId = 1L;
        PaymentMethodEnum paymentMethod = PaymentMethodEnum.VNPAY;
        String ipAddress = "127.0.0.1";

        User user = new User();
        user.setId(userId);
        user.setUsername("TestUser");

        Booking savedBooking = new Booking();
        savedBooking.setId(10L);
        savedBooking.setStatus(BookingStatusEnum.PENDING);
        savedBooking.setUser(user);

        Payment payment = new Payment();
        payment.setId(100L);

        when(userService.getUserById(userId)).thenReturn(user);
        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);
        when(bookingItemService.createListItem(userId, savedBooking)).thenReturn(200000.0);
        when(paymentService.createPayment(any(Booking.class), eq(paymentMethod))).thenReturn(payment);
        when(vnPayService.createPaymentUrl(eq(100L), eq(200000.0), anyString(), eq(ipAddress)))
                .thenReturn("http://vnpay.test/payment");

        // Act
        ResCreateBookingDto result = bookingService.createBooking(userId, paymentMethod, ipAddress);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals("TestUser", result.getUsername());
        assertEquals(200000.0, result.getPrice());
        assertEquals(100L, result.getPaymentId());
        assertEquals("http://vnpay.test/payment", result.getPaymentUrl());

        verify(vnPayService).createPaymentUrl(eq(100L), eq(200000.0), anyString(), eq(ipAddress));
    }

    @Test
    void getBookingById_shouldReturnBooking_whenExists() {
        // Arrange
        Booking booking = new Booking();
        booking.setId(1L);

        when(bookingRepository.findByIdWithDetails(1L)).thenReturn(java.util.Optional.of(booking));

        // Act
        Booking result = bookingService.getBookingById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(bookingRepository).findByIdWithDetails(1L);
    }

    @Test
    void getBookingById_shouldReturnNull_whenNotExists() {
        // Arrange
        when(bookingRepository.findByIdWithDetails(99L)).thenReturn(java.util.Optional.empty());

        // Act
        Booking result = bookingService.getBookingById(99L);

        // Assert
        assertNull(result);
        verify(bookingRepository).findByIdWithDetails(99L);
    }

    @Test
    void deleteBooking_shouldCallRepositoryDeleteById() {
        // Act
        bookingService.deleteBooking(5L);

        // Assert
        verify(bookingRepository).deleteById(5L);
    }

    @Test
    void updateBookingStatus_shouldUpdateStatusAndPaymentToPaid_whenConfirmed() {
        // Arrange
        Long bookingId = 1L;
        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setStatus(BookingStatusEnum.PENDING);

        Payment payment1 = new Payment();
        payment1.setId(100L);
        payment1.setStatus(PaymentStatusEnum.UNPAID);

        Payment payment2 = new Payment();
        payment2.setId(101L);
        payment2.setStatus(PaymentStatusEnum.UNPAID);

        when(bookingRepository.findById(bookingId)).thenReturn(java.util.Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);
        when(paymentService.getPaymentsByBookingId(bookingId)).thenReturn(java.util.List.of(payment1, payment2));

        // Act
        Booking result = bookingService.updateBookingStatus(bookingId, BookingStatusEnum.CONFIRMED);

        // Assert
        assertNotNull(result);
        assertEquals(BookingStatusEnum.CONFIRMED, result.getStatus());
        verify(paymentService).getPaymentsByBookingId(bookingId);
        verify(paymentService, times(2)).savePayment(any(Payment.class));
    }

    @Test
    void updateBookingStatus_shouldNotUpdatePayment_whenNotConfirmed() {
        // Arrange
        Long bookingId = 1L;
        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setStatus(BookingStatusEnum.PENDING);

        when(bookingRepository.findById(bookingId)).thenReturn(java.util.Optional.of(booking));
        when(bookingRepository.save(any(Booking.class))).thenReturn(booking);

        // Act
        Booking result = bookingService.updateBookingStatus(bookingId, BookingStatusEnum.CANCELLED);

        // Assert
        assertNotNull(result);
        assertEquals(BookingStatusEnum.CANCELLED, result.getStatus());
        verify(paymentService, never()).getPaymentsByBookingId(anyLong());
        verify(paymentService, never()).savePayment(any(Payment.class));
    }

    @Test
    void updateBookingStatus_shouldThrowException_whenBookingNotFound() {
        // Arrange
        Long bookingId = 99L;
        when(bookingRepository.findById(bookingId)).thenReturn(java.util.Optional.empty());

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            bookingService.updateBookingStatus(bookingId, BookingStatusEnum.CONFIRMED);
        });
    }
}
