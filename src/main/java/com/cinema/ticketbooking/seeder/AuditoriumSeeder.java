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
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    @Transactional
    public void run(String... args) throws Exception {
        refreshAuditoriumsAndSeats();
    }

    @Transactional
    public void refreshAuditoriumsAndSeats() {
        SeatVariant regularVariant = seatVariantRepository.findBySeatType(SeatTypeEnum.REG)
                .orElseThrow(() -> new RuntimeException("REG seat variant not found"));
        SeatVariant vipVariant = seatVariantRepository.findBySeatType(SeatTypeEnum.VIP)
                .orElseThrow(() -> new RuntimeException("VIP seat variant not found"));

        if (auditoriumRepository.count() == 0) {
            List<Theater> theaters = theaterRepository.findAll();

            for (Theater theater : theaters) {
                for (int i = 1; i <= 3; i++) {
                    Auditorium auditorium = new Auditorium();
                    auditorium.setTheater(theater);
                    auditorium.setNumber((long) i);
                    auditorium.setTotalSeats(0L);
                    auditoriumRepository.save(auditorium);
                }
            }
        }

        int updatedAuditoriums = 0;
        int createdSeats = 0;
        for (Auditorium auditorium : auditoriumRepository.findAll()) {
            List<Seat> existingSeats = seatRepository.findByAuditoriumId(auditorium.getId());
            Map<String, Seat> seatsByPosition = new HashMap<>();
            for (Seat seat : existingSeats) {
                seatsByPosition.putIfAbsent(seat.getSeatRow() + "-" + seat.getNumber(), seat);
            }

            List<Seat> seatsToSave = new ArrayList<>();
            int newSeatsForAuditorium = 0;
            char[] rows = { 'A', 'B', 'C', 'D', 'E', 'F' };
            for (char row : rows) {
                for (int seatNum = 1; seatNum <= 8; seatNum++) {
                    String position = row + "-" + seatNum;
                    Seat seat = seatsByPosition.get(position);
                    SeatVariant expectedVariant = row == 'F' ? vipVariant : regularVariant;
                    if (seat == null) {
                        seat = new Seat();
                        seat.setAuditorium(auditorium);
                        seat.setSeatRow(String.valueOf(row));
                        seat.setNumber(seatNum);
                        seat.setSeatVariant(expectedVariant);
                        seatsToSave.add(seat);
                        createdSeats++;
                        newSeatsForAuditorium++;
                    } else if (seat.getSeatVariant() == null) {
                        seat.setSeatVariant(expectedVariant);
                        seatsToSave.add(seat);
                    }
                }
            }

            if (!seatsToSave.isEmpty()) {
                seatRepository.saveAll(seatsToSave);
                auditorium.setTotalSeats((long) existingSeats.size() + newSeatsForAuditorium);
                auditoriumRepository.save(auditorium);
                updatedAuditoriums++;
            }
        }

        System.out.println("Refreshed seats: added " + createdSeats + " seats across "
                + updatedAuditoriums + " auditoriums");
    }
}
