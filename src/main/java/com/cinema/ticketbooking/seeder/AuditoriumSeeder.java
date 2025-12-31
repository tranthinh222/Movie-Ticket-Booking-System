package com.cinema.ticketbooking.seeder;

import com.cinema.ticketbooking.domain.Theater;
import com.cinema.ticketbooking.repository.AuditoriumRepository;
import com.cinema.ticketbooking.repository.TheaterRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuditoriumSeeder implements CommandLineRunner {
    private final AuditoriumRepository auditoriumRepository;
    private final TheaterRepository theaterRepository;
    public AuditoriumSeeder (AuditoriumRepository auditoriumRepository, TheaterRepository theaterRepository) {
        this.auditoriumRepository = auditoriumRepository;
        this.theaterRepository = theaterRepository;
    }
    @Override
    public void run(String... args) throws Exception {
        if (auditoriumRepository.count() == 0){
            List<Theater> theaters = theaterRepository.findAll();

        }
        else {
            System.out.println("Auditorium already exists");
        }
    }
}
