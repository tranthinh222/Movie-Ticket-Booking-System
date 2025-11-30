package com.cinema.ticketbooking.controller;

import com.cinema.ticketbooking.domain.User;
import com.cinema.ticketbooking.domain.response.ResUpdateUserDto;
import com.cinema.ticketbooking.domain.response.ResUserDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.service.UserService;
import com.cinema.ticketbooking.util.annotation.ApiMessage;
import com.cinema.ticketbooking.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/v1")
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserController(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }


    @GetMapping("/users/{id}")
    @ApiMessage("fetch user by id")
    public ResponseEntity<ResUserDto> getUserById(@PathVariable long id) throws IdInvalidException {
        User fetchUser = this.userService.getUserById(id);
        if (fetchUser == null){
            throw new IdInvalidException("User with id " + id + " not found");
        }

        return ResponseEntity.status(HttpStatus.OK).body(this.userService.convertToResUserDTO(fetchUser));
    }

    @GetMapping("/users")
    @ApiMessage("fetch all users")
    public ResponseEntity<ResultPaginationDto> getAllUsers(
            @Filter Specification<User> spec, Pageable pageable
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.getAllUser(spec, pageable));
    }

    @PostMapping("/users")
    @ApiMessage("create a user")
    public ResponseEntity<User> createUser(@RequestBody User user) throws Exception {
        boolean isEmailExist = this.userService.existsByEmail(user.getEmail());
        if (isEmailExist){
            throw new Exception("User with email " + user.getEmail() + " already exists");
        }
        String hashPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashPassword);
        this.userService.createUser(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PutMapping("/users")
    @ApiMessage("update a user")
    public ResponseEntity<ResUpdateUserDto> updateUser(@RequestBody User user) throws Exception {
        boolean isEmailExist = this.userService.existsByEmail(user.getEmail());
        if (isEmailExist){
            throw new Exception("User with email " + user.getEmail() + " already exists");
        }
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.updateUser(user));

    }

    @DeleteMapping("/users")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") long id) throws Exception{
        User user = this.userService.getUserById(id);
        if (user == null){
            throw new Exception("User with id " + id + " already exists");
        }

        this.userService.deleteUser(id);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
}
