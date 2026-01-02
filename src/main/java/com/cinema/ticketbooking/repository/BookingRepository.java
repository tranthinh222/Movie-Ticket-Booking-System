package com.cinema.ticketbooking.repository;

import org.springframework.stereotype.Repository;

import com.cinema.ticketbooking.domain.Booking;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>, JpaSpecificationExecutor<Booking> {

    @Query("SELECT DISTINCT b FROM Booking b " +
            "LEFT JOIN FETCH b.user " +
            "LEFT JOIN FETCH b.bookingItems bi " +
            "LEFT JOIN FETCH bi.seat s " +
            "LEFT JOIN FETCH bi.showTime st " +
            "LEFT JOIN FETCH st.film " +
            "LEFT JOIN FETCH st.auditorium a " +
            "LEFT JOIN FETCH a.theater t " +
            "LEFT JOIN FETCH t.address " +
            "WHERE b.id = :id")
    Optional<Booking> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT b.id FROM Booking b " +
            "WHERE b.user.id = :userId " +
            "ORDER BY b.createdAt DESC")
    Page<Long> findIdsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT DISTINCT b FROM Booking b " +
            "LEFT JOIN FETCH b.user " +
            "LEFT JOIN FETCH b.bookingItems bi " +
            "LEFT JOIN FETCH bi.seat s " +
            "LEFT JOIN FETCH bi.showTime st " +
            "LEFT JOIN FETCH st.film " +
            "LEFT JOIN FETCH st.auditorium a " +
            "LEFT JOIN FETCH a.theater t " +
            "LEFT JOIN FETCH t.address " +
            "WHERE b.id IN :ids " +
            "ORDER BY b.createdAt DESC")
    java.util.List<Booking> findByIdsWithDetails(@Param("ids") java.util.List<Long> ids);
}
