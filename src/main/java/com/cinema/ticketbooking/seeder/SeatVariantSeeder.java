package com.cinema.ticketbooking.seeder;

import com.cinema.ticketbooking.domain.SeatVariant;
import com.cinema.ticketbooking.repository.SeatVariantRepository;
import com.cinema.ticketbooking.util.constant.SeatTypeEnum;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(2)
public class SeatVariantSeeder implements CommandLineRunner {
    private final SeatVariantRepository seatVariantRepository;

    public SeatVariantSeeder(SeatVariantRepository seatVariantRepository) {
        this.seatVariantRepository = seatVariantRepository;
    }

    @Override
    public void run(String... args) {
        if (seatVariantRepository.count() == 0) {
            SeatVariant regularSeat = new SeatVariant();
            regularSeat.setSeatType(SeatTypeEnum.REG);
            regularSeat.setBasePrice(70000);
            regularSeat.setBonus(0);

            SeatVariant vipSeat = new SeatVariant();
            vipSeat.setSeatType(SeatTypeEnum.VIP);
            vipSeat.setBasePrice(70000);
            vipSeat.setBonus(10000);

            seatVariantRepository.saveAll(List.of(regularSeat, vipSeat));

            System.out.println("Seeded Seat Variants");
        } else {
            System.out.println("Seat Variants already exist");
        }
    }
}
