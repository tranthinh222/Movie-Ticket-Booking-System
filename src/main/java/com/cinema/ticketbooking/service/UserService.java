package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.User;
import com.cinema.ticketbooking.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserById(long id){
        return this.userRepository.findById(id).orElse(null);
    }


    public List<User> getAllUser(){
        return this.userRepository.findAll();
    }

    public User createUser(User user){
        return this.userRepository.save(user);
    }

    public User getUserByEmail(String email){
        return this.userRepository.findUserByEmail(email);
    }


}
