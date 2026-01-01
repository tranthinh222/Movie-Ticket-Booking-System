package com.cinema.ticketbooking.repository.projection;

import com.cinema.ticketbooking.domain.Address;

public interface TheaterIdNameProjection {
    Long getId();
    String getName();
    Address getAddress();
}
