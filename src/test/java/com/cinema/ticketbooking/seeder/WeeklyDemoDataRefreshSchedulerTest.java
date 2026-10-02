package com.cinema.ticketbooking.seeder;

import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.mockito.Mockito.*;

class WeeklyDemoDataRefreshSchedulerTest {
    @Test
    void refreshesVariantsSeatsAndShowtimesInDependencyOrder() {
        SeatVariantSeeder variants = mock(SeatVariantSeeder.class);
        AuditoriumSeeder auditoriums = mock(AuditoriumSeeder.class);
        LocalShowTimeSeeder showtimes = mock(LocalShowTimeSeeder.class);

        new WeeklyDemoDataRefreshScheduler(variants, auditoriums, showtimes)
                .refreshWeeklyDemoData();

        InOrder order = inOrder(variants, auditoriums, showtimes);
        order.verify(variants).refreshDefaultSeatVariants();
        order.verify(auditoriums).refreshAuditoriumsAndSeats();
        order.verify(showtimes).refreshWeeklySchedule();
    }
}
