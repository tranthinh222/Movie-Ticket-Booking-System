package com.cinema.ticketbooking.repository;

import com.cinema.ticketbooking.domain.Address;
import com.cinema.ticketbooking.repository.projection.AddressWithTheatersProjection;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long>, JpaSpecificationExecutor<Address> {
    Optional<AddressWithTheatersProjection> findProjectedById(Long id);
}
