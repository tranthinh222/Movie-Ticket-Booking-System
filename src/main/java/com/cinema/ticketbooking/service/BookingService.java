package com.cinema.ticketbooking.service;

import org.springframework.stereotype.Service;

import com.cinema.ticketbooking.domain.Booking;
import com.cinema.ticketbooking.domain.User;
import com.cinema.ticketbooking.domain.request.ReqCreateBookingDto;
import com.cinema.ticketbooking.repository.BookingRepository;
import com.cinema.ticketbooking.util.constant.BookingStatusEnum;

@Service
public class BookingService {
    private final BookingRepository bookingRepo;
    private final UserService userService;
    private final BookingItemService bookingItemService;

    BookingService(BookingRepository bookingRepo, UserService userService, BookingItemService bookingItemService) {
        this.bookingRepo = bookingRepo;
        this.userService = userService;
        this.bookingItemService = bookingItemService;
    }

    public Booking createBooking(ReqCreateBookingDto reqBooking) {
        Booking booking = new Booking();
        User user = this.userService.getUserById(reqBooking.getUserId());

        booking.setUser(user);
        booking.setStatus(BookingStatusEnum.PENDING);
        Booking savedBooking = this.bookingRepo.save(booking);

        Double total_price = this.bookingItemService.createListItem(reqBooking.getUserId(), savedBooking);

        booking.setTotal_price(total_price);
        return savedBooking;
    }
}
