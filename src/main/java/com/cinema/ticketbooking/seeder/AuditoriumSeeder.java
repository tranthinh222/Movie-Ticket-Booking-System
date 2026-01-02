package com.cinema.ticketbooking.seeder;

import com.cinema.ticketbooking.domain.Auditorium;
import com.cinema.ticketbooking.domain.Seat;
import com.cinema.ticketbooking.domain.SeatVariant;
import com.cinema.ticketbooking.domain.Theater;
import com.cinema.ticketbooking.repository.AuditoriumRepository;
import com.cinema.ticketbooking.repository.SeatRepository;
import com.cinema.ticketbooking.repository.SeatVariantRepository;
import com.cinema.ticketbooking.repository.TheaterRepository;
import com.cinema.ticketbooking.util.constant.SeatTypeEnum;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Order(4)
public class AuditoriumSeeder implements CommandLineRunner {
    private final AuditoriumRepository auditoriumRepository;
    private final TheaterRepository theaterRepository;
    private final SeatRepository seatRepository;
    private final SeatVariantRepository seatVariantRepository;

    public AuditoriumSeeder(AuditoriumRepository auditoriumRepository,
            TheaterRepository theaterRepository,
            SeatRepository seatRepository,
            SeatVariantRepository seatVariantRepository) {
        this.auditoriumRepository = auditoriumRepository;
        this.theaterRepository = theaterRepository;
        this.seatRepository = seatRepository;
        this.seatVariantRepository = seatVariantRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        if (auditoriumRepository.count() == 0) {
            // Lấy SeatVariant REG và VIP
            SeatVariant regularVariant = seatVariantRepository.findBySeatType(SeatTypeEnum.REG)
                    .orElseThrow(() -> new RuntimeException("REG seat variant not found"));
            SeatVariant vipVariant = seatVariantRepository.findBySeatType(SeatTypeEnum.VIP)
                    .orElseThrow(() -> new RuntimeException("VIP seat variant not found"));

            List<Theater> theaters = theaterRepository.findAll();

            for (Theater theater : theaters) {
                for (int i = 1; i <= 3; i++) {
                    Auditorium auditorium = new Auditorium();
                    auditorium.setTheater(theater);
                    auditorium.setNumber((long) i);
                    auditorium.setTotalSeats(48L);
                    auditoriumRepository.save(auditorium);

                    // Tạo seats cho auditorium (6 hàng x 8 ghế = 48 ghế)
                    List<Seat> seats = new ArrayList<>();
                    char[] rows = { 'A', 'B', 'C', 'D', 'E', 'F' };

                    for (char row : rows) {
                        for (int seatNum = 1; seatNum <= 8; seatNum++) {
                            Seat seat = new Seat();
                            seat.setAuditorium(auditorium);
                            seat.setSeatRow(String.valueOf(row));
                            seat.setNumber(seatNum);

                            // Hàng F là VIP, các hàng khác là REG
                            if (row == 'F') {
                                seat.setSeatVariant(vipVariant);
                            } else {
                                seat.setSeatVariant(regularVariant);
                            }

                            seats.add(seat);
                        }
                    }

                    seatRepository.saveAll(seats);
                }
            }

            System.out.println("Seeded Auditoriums and Seats");
        } else {
            System.out.println("Auditorium already exists");
        }
    }
}
