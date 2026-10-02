package com.cinema.ticketbooking.seeder;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Refreshes dependent demo data in a deterministic order every week. */
@Component
public class WeeklyDemoDataRefreshScheduler {
    private final SeatVariantSeeder seatVariantSeeder;
    private final AuditoriumSeeder auditoriumSeeder;
    private final LocalShowTimeSeeder showTimeSeeder;

    public WeeklyDemoDataRefreshScheduler(SeatVariantSeeder seatVariantSeeder,
            AuditoriumSeeder auditoriumSeeder, LocalShowTimeSeeder showTimeSeeder) {
        this.seatVariantSeeder = seatVariantSeeder;
        this.auditoriumSeeder = auditoriumSeeder;
        this.showTimeSeeder = showTimeSeeder;
    }

    @Scheduled(cron = "0 15 23 * * FRI", zone = "Asia/Ho_Chi_Minh")
    public void refreshWeeklyDemoData() {
        seatVariantSeeder.refreshDefaultSeatVariants();
        auditoriumSeeder.refreshAuditoriumsAndSeats();
        showTimeSeeder.refreshWeeklySchedule();
    }
}
