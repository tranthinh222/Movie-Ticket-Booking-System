package com.cinema.ticketbooking.seeder;

import com.cinema.ticketbooking.domain.Address;
import com.cinema.ticketbooking.repository.AddressRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Order(1)
public class AddressSeeder implements CommandLineRunner {
    private final AddressRepository addressRepository;
    public AddressSeeder(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    @Override
    public void run(String... args) {
        if (addressRepository.count() == 0) {
            Address addr1 = Address.builder()
                    .street_number("123")
                    .street_name("Le Loi")
                    .city("Ho Chi Minh")
                    .build();

            Address addr2 = Address.builder()
                    .street_number("456")
                    .street_name("Nguyen Hue")
                    .city("Ho Chi Minh")
                    .build();

            Address addr3 = Address.builder()
                    .street_number("789")
                    .street_name("Tran Hung Dao")
                    .city("Ha Noi")
                    .build();

            addressRepository.saveAll(List.of(addr1, addr2, addr3));
            System.out.println("Seeded Addresses");
        }
        else {
            System.out.println("Addresses already exist");
        }
    }
}
