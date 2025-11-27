package com.cinema.ticketbooking.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.cinema.ticketbooking.domain.dto.LoginDto;
import com.cinema.ticketbooking.domain.dto.RegisterDto;
import com.cinema.ticketbooking.domain.dto.ResLoginDto;
import com.cinema.ticketbooking.domain.dto.ResRegisterDto;
import com.cinema.ticketbooking.service.AuthService;

@RestController
@RequestMapping("api/v1")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ResLoginDto> login(@Valid @RequestBody LoginDto loginDto) {
        ResLoginDto response = authService.login(loginDto);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/register")
    public ResponseEntity<ResRegisterDto> register(@Valid @RequestBody RegisterDto registerDto) {
        ResRegisterDto response = authService.register(registerDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
