package com.cinema.ticketbooking.repository;

import org.springframework.stereotype.Repository;

import com.cinema.ticketbooking.domain.Booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {

}
