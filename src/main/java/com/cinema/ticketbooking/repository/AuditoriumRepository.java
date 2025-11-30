package com.cinema.ticketbooking.repository;

import com.cinema.ticketbooking.domain.Auditorium;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository 
public interface AuditoriumRepository extends JpaRepository<Auditorium, Long>, JpaSpecificationExecutor<Auditorium> {
}
