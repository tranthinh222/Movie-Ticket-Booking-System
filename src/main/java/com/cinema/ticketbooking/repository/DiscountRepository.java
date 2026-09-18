package com.cinema.ticketbooking.repository;
import com.cinema.ticketbooking.domain.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface DiscountRepository extends JpaRepository<Discount,Long> {
 Optional<Discount> findByCode(String code);
 Optional<Discount> findBySeedKey(String seedKey);
 List<Discount> findByActiveTrueAndStartsOnLessThanEqualAndEndsOnGreaterThanEqualOrderByIdDesc(java.time.LocalDate start,java.time.LocalDate end);
}
