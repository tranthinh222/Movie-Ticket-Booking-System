package com.cinema.ticketbooking.repository;

import com.cinema.ticketbooking.domain.SeatVariant;
import com.cinema.ticketbooking.domain.ShowTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface SeatVariantRepository extends JpaRepository<SeatVariant, Long>, JpaSpecificationExecutor<SeatVariant> {
}
