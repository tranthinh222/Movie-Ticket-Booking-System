package com.cinema.ticketbooking.repository.projection;

import java.util.List;

public interface AddressWithTheatersProjection {
    List<TheaterIdNameProjection> getTheaters();
}
