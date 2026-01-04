package com.cinema.ticketbooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.cinema.ticketbooking.domain.BookingItem;

public interface BookingItemRepository extends JpaRepository<BookingItem, Long>, JpaSpecificationExecutor<BookingItem> {

    /**
     * Kiểm tra ghế đã được booking cho showtime cụ thể chưa
     */
    boolean existsBySeatIdAndShowTimeId(Long seatId, Long showTimeId);

    /**
     * Kiểm tra có booking nào cho showtime không
     */
    boolean existsByShowTimeId(Long showTimeId);

}
