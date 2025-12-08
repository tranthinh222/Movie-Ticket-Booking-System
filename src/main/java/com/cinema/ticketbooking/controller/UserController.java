package com.cinema.ticketbooking.controller;

import com.cinema.ticketbooking.domain.User;
import com.cinema.ticketbooking.domain.request.ReqCreateUserDto;
import com.cinema.ticketbooking.domain.request.ReqUpdateUserDto;
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
    @ApiMessage("create an user")
    public ResponseEntity<User> createUser(@RequestBody ReqCreateUserDto user) throws Exception {
        boolean isEmailExist = this.userService.existsByEmail(user.getEmail());
        if (isEmailExist){
            throw new Exception("User with email " + user.getEmail() + " already exists");
        }
        String hashPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashPassword);

        return ResponseEntity.status(HttpStatus.CREATED).body(this.userService.createUser(user));
    }

    @PutMapping("/users")
    @ApiMessage("update an user")
    public ResponseEntity<ResUpdateUserDto> updateUser(@RequestBody ReqUpdateUserDto reqUser) {
        User user = this.userService.getUserById(reqUser.getId());
        if (user == null){
            throw new IdInvalidException("User with id " + reqUser.getId() + " does not exist");
        }
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.updateUser(reqUser));

    }

    @DeleteMapping("/users")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") long id) {
        User user = this.userService.getUserById(id);
        if (user == null){
            throw new IdInvalidException("User with id " + id + " does not exist");
        }

        this.userService.deleteUser(id);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
}
