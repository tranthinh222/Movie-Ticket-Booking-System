package com.cinema.ticketbooking.domain;

import com.cinema.ticketbooking.repository.SeatHoldRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Scheduler để tự động xóa các SeatHold đã hết hạn (quá 5 phút)
 * Không cần cập nhật Seat status vì ghế không còn có trạng thái toàn cục nữa
 */
@Component
public class SeatHoldCleanupScheduler {

    @Autowired
    private SeatHoldRepository seatHoldRepository;

    @Scheduled(fixedRate = 60000) // Chạy mỗi 1 phút
    @Transactional
    public void cleanupExpiredSeatHolds() {
        Instant now = Instant.now();
        List<SeatHold> expiredHolds = seatHoldRepository.findExpiredSeatHolds(now);

        if (!expiredHolds.isEmpty()) {
            seatHoldRepository.deleteAll(expiredHolds);
            System.out.println("Cleanup at " + now + ": Deleted " + expiredHolds.size() + " expired SeatHolds.");
        }
    }
}