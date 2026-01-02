package com.cinema.ticketbooking.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.cinema.ticketbooking.domain.SeatHold;

@Repository
public interface SeatHoldRepository extends JpaRepository<SeatHold, Long>, JpaSpecificationExecutor<SeatHold> {
    @Query("SELECT sh FROM SeatHold sh LEFT JOIN FETCH sh.seat s WHERE sh.expiresAt IS NOT NULL AND sh.expiresAt < :now")
    List<SeatHold> findExpiredSeatHolds(@Param("now") Instant now);

    @Query("""
                SELECT sh FROM SeatHold sh
                LEFT JOIN FETCH sh.seat
                LEFT JOIN FETCH sh.showTime
                WHERE sh.user.id = :userId
            """)
    List<SeatHold> findByUserIdFetchFull(@Param("userId") Long userId);

    /**
     * Kiểm tra ghế đã được hold cho showtime cụ thể chưa
     */
    boolean existsBySeatIdAndShowTimeId(Long seatId, Long showTimeId);

}
