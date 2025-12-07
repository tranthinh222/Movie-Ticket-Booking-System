package com.cinema.ticketbooking.repository;

import com.cinema.ticketbooking.domain.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long>, JpaSpecificationExecutor<Seat> {
}
