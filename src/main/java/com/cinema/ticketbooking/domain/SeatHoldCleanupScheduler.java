package com.cinema.ticketbooking.domain;

import com.cinema.ticketbooking.repository.SeatHoldRepository;
import com.cinema.ticketbooking.repository.SeatRepository;
import com.cinema.ticketbooking.util.constant.SeatStatusEnum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
public class SeatHoldCleanupScheduler {

    @Autowired
    private SeatHoldRepository seatHoldRepository;

    @Autowired
    private SeatRepository seatRepository;

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cleanupExpiredSeatHolds() {
        Instant now = Instant.now();
        List<SeatHold> expiredHolds = seatHoldRepository.findExpiredSeatHolds(now);

        int deletedCount = 0;
        int updatedSeatsCount = 0;

        for (SeatHold hold : expiredHolds) {
            Seat seat = hold.getSeat();
            if (seat != null && seat.getStatus() == SeatStatusEnum.HOLD) {
                seat.setStatus(SeatStatusEnum.AVAILABLE);
                seatRepository.save(seat);
                updatedSeatsCount++;
            }
            seatHoldRepository.delete(hold);
            deletedCount++;
        }

        // if (deletedCount > 0) {
        // System.out.println("Cleanup lúc " + now + ": Đã xóa " + deletedCount + "
        // SeatHold. " +
        // "Khôi phục " + updatedSeatsCount + " Seat từ HOLD sang AVAILABLE.");
        // }
    }
}