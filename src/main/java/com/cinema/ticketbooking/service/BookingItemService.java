package com.cinema.ticketbooking.service;

import java.util.List;

import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cinema.ticketbooking.domain.Booking;
import com.cinema.ticketbooking.domain.BookingItem;
import com.cinema.ticketbooking.domain.Seat;
import com.cinema.ticketbooking.domain.SeatHold;
import com.cinema.ticketbooking.domain.SeatVariant;
import com.cinema.ticketbooking.repository.BookingItemRepository;
import com.cinema.ticketbooking.repository.SeatHoldRepository;
import com.cinema.ticketbooking.repository.SeatRepository;
import com.cinema.ticketbooking.util.constant.SeatStatusEnum;
import com.cinema.ticketbooking.util.error.NoResourceException;

@Service
public class BookingItemService {
    private final BookingItemRepository bookingItemRepo;
    private final SeatHoldService seatHoldService;
    private final SeatRepository seatRepository;
    private final SeatHoldRepository seatHoldRepository;

    BookingItemService(BookingItemRepository bookingItemRepo, SeatHoldService seatHoldService,
            SeatRepository seatRepository, SeatHoldRepository seatHoldRepository) {
        this.bookingItemRepo = bookingItemRepo;
        this.seatHoldService = seatHoldService;
        this.seatRepository = seatRepository;
        this.seatHoldRepository = seatHoldRepository;
    }

    @Transactional
    public Double createListItem(Long userId, Booking booking) {
        List<SeatHold> listSeatHold = this.seatHoldService.getSeatHoldByUserId(userId);
        if (listSeatHold.size() == 0)
            throw new NoResourceException("Ghế giữ quá thời gian hoặc không khả dụng vui lòng chọn và đặt ghế khác");
        Double sum = 0.0;
        for (SeatHold seatHold : listSeatHold) {
            Seat seat = seatHold.getSeat();

            // Tạo booking item với showtime
            BookingItem item = new BookingItem();
            item.setBooking(booking);
            item.setSeat(seat);
            item.setShowTime(seatHold.getShowTime()); // Thêm showtime
            SeatVariant seatVariant = seat.getSeatVariant();
            // Tính giá = giá ghế + giá phim
            Long filmPrice = seatHold.getShowTime().getFilm().getPrice();
            item.setPrice(seatVariant.getBasePrice() + seatVariant.getBonus() + filmPrice);
            this.bookingItemRepo.save(item);
            sum += item.getPrice();
        }

        // Xóa các SeatHold sau khi đã booking thành công
        seatHoldRepository.deleteAll(listSeatHold);

        return sum;
    }
}
