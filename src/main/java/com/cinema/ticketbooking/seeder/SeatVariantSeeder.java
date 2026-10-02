package com.cinema.ticketbooking.seeder;

import com.cinema.ticketbooking.domain.SeatVariant;
import com.cinema.ticketbooking.repository.SeatVariantRepository;
import com.cinema.ticketbooking.util.constant.SeatTypeEnum;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
        refreshDefaultSeatVariants();
    }

    @Transactional
    public void refreshDefaultSeatVariants() {
        SeatVariant regularSeat = seatVariantRepository.findBySeatType(SeatTypeEnum.REG)
                .orElseGet(SeatVariant::new);
        regularSeat.setSeatType(SeatTypeEnum.REG);
        regularSeat.setBasePrice(70000);
        regularSeat.setBonus(0);

        SeatVariant vipSeat = seatVariantRepository.findBySeatType(SeatTypeEnum.VIP)
                .orElseGet(SeatVariant::new);
        vipSeat.setSeatType(SeatTypeEnum.VIP);
        vipSeat.setBasePrice(70000);
        vipSeat.setBonus(10000);

        seatVariantRepository.saveAll(List.of(regularSeat, vipSeat));
        System.out.println("Refreshed default seat variants");
    }
}
