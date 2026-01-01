package com.cinema.ticketbooking.seeder;

import com.cinema.ticketbooking.domain.User;
import com.cinema.ticketbooking.repository.UserRepository;
import com.cinema.ticketbooking.util.constant.RoleEnum;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class UserSeeder implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    public UserSeeder(UserRepository userRepository,  PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            seedAdmin();
            seedCustomer();
        }
        else {
            System.out.println("Users already exist");
        }
    }

    public void seedAdmin (){
        for (int i = 1; i <= 5; i++)
        {
            String username = "admin" + i;
            String email = "admin" + i + "@gmail.com";
            String hashPassword = passwordEncoder.encode("123456");
            String phone = "097690845" + i;
            User admin = User.builder()
                    .username(username)
                    .email(email)
                    .password(hashPassword)
                    .phone(phone)
                    .role(RoleEnum.ADMIN).build();
            userRepository.save(admin);
        }
    }

    public void seedCustomer (){
        for (int i = 1; i <= 5; i++)
        {
            String username = "customer" + i;
            String email = "customer" + i + "@gmail.com";
            String hashPassword = passwordEncoder.encode("123456");
            String phone = "098290845" + i;
            User admin = User.builder()
                    .username(username)
                    .email(email)
                    .password(hashPassword)
                    .role(RoleEnum.CUSTOMER)
                    .phone(phone).build();
            userRepository.save(admin);
        }
    }
}
