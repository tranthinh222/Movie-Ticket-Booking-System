package com.cinema.ticketbooking.controller;

import com.cinema.ticketbooking.domain.response.ResUserJwtDto;
import com.cinema.ticketbooking.util.annotation.ApiMessage;
import com.cinema.ticketbooking.util.error.ApiException;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.cinema.ticketbooking.domain.request.ReqLoginDto;
import com.cinema.ticketbooking.domain.request.ReqRegisterDto;
import com.cinema.ticketbooking.domain.response.ResLoginDto;
import com.cinema.ticketbooking.domain.response.ResUserDto;
import com.cinema.ticketbooking.service.AuthService;

@RestController
@RequestMapping("api/v1/auth")
public class AuthController {
    private final AuthService authService;
    @Value("${ticketbooking.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<ResLoginDto> login(@Valid @RequestBody ReqLoginDto reqLoginDto) {
        ResLoginDto response = this.authService.login(reqLoginDto);
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", response.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(response);
    }

    @PostMapping("/register")
    public ResponseEntity<ResUserDto> register(@Valid @RequestBody ReqRegisterDto reqRegisterDto) {
        ResUserDto response = authService.register(reqRegisterDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/account")
    @ApiMessage("fetch account")
    public ResponseEntity<ResUserJwtDto> getAccount() {
        return ResponseEntity.ok().body(this.authService.getAccount());
    }

    @GetMapping("/refresh")
    @ApiMessage("Get new refresh token")
    public ResponseEntity<ResLoginDto> getRefreshToken(
            @CookieValue(name = "refresh_token", required = false) String refreshToken) {
        if (refreshToken == null) {
            throw new ApiException("Missing refresh token", HttpStatus.UNAUTHORIZED);
        }
        return this.authService.getRefreshToken(refreshToken);
    }

}
