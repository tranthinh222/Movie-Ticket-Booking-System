package com.cinema.ticketbooking.repository;

import com.cinema.ticketbooking.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findUserById(long id);

    User findUserByEmail(String email);

    boolean existsByEmail(String email);
}
