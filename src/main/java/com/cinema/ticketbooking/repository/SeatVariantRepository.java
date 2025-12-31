package com.cinema.ticketbooking.repository;

import com.cinema.ticketbooking.domain.SeatVariant;
import com.cinema.ticketbooking.util.constant.SeatTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface SeatVariantRepository extends JpaRepository<SeatVariant, Long>, JpaSpecificationExecutor<SeatVariant> {

    Optional<SeatVariant> findBySeatType(SeatTypeEnum seatType);
}
