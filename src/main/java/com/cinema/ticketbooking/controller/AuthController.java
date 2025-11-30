package com.cinema.ticketbooking.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.cinema.ticketbooking.domain.request.ReqLoginDto;
import com.cinema.ticketbooking.domain.request.ReqRegisterDto;
import com.cinema.ticketbooking.domain.response.ResLoginDto;
import com.cinema.ticketbooking.domain.response.ResRegisterDto;
import com.cinema.ticketbooking.service.AuthService;

@RestController
@RequestMapping("api/v1")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ResLoginDto> login(@Valid @RequestBody ReqLoginDto reqLoginDto) {
        return this.authService.login(reqLoginDto);
    }

    @PostMapping("/register")
    public ResponseEntity<ResRegisterDto> register(@Valid @RequestBody ReqRegisterDto reqRegisterDto) {
        ResRegisterDto response = authService.register(reqRegisterDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}
