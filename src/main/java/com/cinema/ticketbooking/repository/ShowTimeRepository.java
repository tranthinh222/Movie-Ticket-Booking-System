package com.cinema.ticketbooking.repository;

import com.cinema.ticketbooking.domain.ShowTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ShowTimeRepository extends JpaRepository<ShowTime, Long>, JpaSpecificationExecutor<ShowTime> {
    @org.springframework.data.jpa.repository.Query("select s from ShowTime s join fetch s.film where (:date is null or s.date = :date) and s.startTime is not null and (s.date > :today or (s.date = :today and s.startTime > :time)) order by s.date, s.startTime, s.id")
    java.util.List<ShowTime> findRecommendationCandidates(
            @org.springframework.data.repository.query.Param("date") java.time.LocalDate date,
            @org.springframework.data.repository.query.Param("today") java.time.LocalDate today,
            @org.springframework.data.repository.query.Param("time") java.time.LocalTime time);

    java.util.List<ShowTime> findAllByDateBetween(java.time.LocalDate from, java.time.LocalDate to);
}
