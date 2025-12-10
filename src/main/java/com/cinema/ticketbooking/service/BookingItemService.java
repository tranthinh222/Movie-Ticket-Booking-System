package com.cinema.ticketbooking.service;

import java.util.List;

import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import com.cinema.ticketbooking.domain.Booking;
import com.cinema.ticketbooking.domain.BookingItem;
import com.cinema.ticketbooking.domain.SeatHold;
import com.cinema.ticketbooking.domain.SeatVariant;
import com.cinema.ticketbooking.repository.BookingItemRepository;
import com.cinema.ticketbooking.util.error.NoResourceException;

@Service
public class BookingItemService {
    private final BookingItemRepository bookingItemRepo;
    private final SeatHoldService seatHoldService;

    BookingItemService(BookingItemRepository bookingItemRepo, SeatHoldService seatHoldService) {
        this.bookingItemRepo = bookingItemRepo;
        this.seatHoldService = seatHoldService;
    }

    public Double createListItem(Long userId, Booking booking) {
        List<SeatHold> listSeatHold = this.seatHoldService.getSeatHoldByUserId(userId);
        if (listSeatHold.size() == 0)
            throw new NoResourceException("Ghế giữ quá thời gian hoặc không khả dụng vui lòng chọn và đặt ghế khác");
        Double sum = 0.0;
        for (SeatHold seat : listSeatHold) {
            BookingItem item = new BookingItem();
            item.setBooking(booking);
            SeatVariant seatVariant = seat.getSeat().getSeatVariant();
            item.setPrice(seatVariant.getBasePrice() + seatVariant.getBonus());
            item.setSeat(seat.getSeat());
            this.bookingItemRepo.save(item);
            sum += item.getPrice();
        }
        return sum;
    }
}
