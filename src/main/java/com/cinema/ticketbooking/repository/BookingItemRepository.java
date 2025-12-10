package com.cinema.ticketbooking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.cinema.ticketbooking.domain.BookingItem;

public interface BookingItemRepository extends JpaRepository<BookingItem, Long>, JpaSpecificationExecutor<BookingItem> {

}
