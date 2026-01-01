package com.cinema.ticketbooking.seeder;

import com.cinema.ticketbooking.domain.Address;
import com.cinema.ticketbooking.domain.Theater;
import com.cinema.ticketbooking.repository.AddressRepository;
import com.cinema.ticketbooking.repository.TheaterRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(3)
public class TheaterSeeder implements CommandLineRunner {
    private final TheaterRepository theaterRepository;
    private final AddressRepository addressRepository;
    public TheaterSeeder(TheaterRepository theaterRepository,  AddressRepository addressRepository) {
        this.theaterRepository = theaterRepository;
        this.addressRepository = addressRepository;
    }

    @Override
    public void run(String... args) {
        if (theaterRepository.count() == 0) {
            List<Address> addresses = this.addressRepository.findAll();
            Theater t1 = Theater.builder()
                    .name("Galaxy Cinema Lê Lợi")
                    .address(addresses.get(0))
                    .build();

            Theater t2 = Theater.builder()
                    .name("CGV Nguyễn Huệ")
                    .address(addresses.get(1))
                    .build();

            Theater t3 = Theater.builder()
                    .name("Lotte Cinema Trần Hưng Đạo")
                    .address(addresses.get(2))
                    .build();

            Theater t4 = Theater.builder()
                    .name("BHD Star Nguyễn Huệ")
                    .address(addresses.get(1))
                    .build();

            Theater t5 = Theater.builder()
                    .name("MegaStar Lê Lợi")
                    .address(addresses.get(0))
                    .build();

            theaterRepository.saveAll(List.of(t1, t2, t3, t4, t5));
            System.out.println("Seeded Theaters");
        }
        else {
            System.out.println("Theater already exist");
        }
    }
}
