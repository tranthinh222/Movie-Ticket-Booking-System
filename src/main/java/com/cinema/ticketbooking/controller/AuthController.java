package com.cinema.ticketbooking.controller;

import com.cinema.ticketbooking.domain.User;
import com.cinema.ticketbooking.domain.response.ResUserJwtDto;
import com.cinema.ticketbooking.service.UserService;
import com.cinema.ticketbooking.util.SecurityUtil;
import com.cinema.ticketbooking.util.annotation.ApiMessage;
import com.cinema.ticketbooking.util.error.ApiException;
import com.cinema.ticketbooking.util.error.IdInvalidException;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
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

    @PostMapping("/auth/login")
    public ResponseEntity<ResLoginDto> login(@Valid @RequestBody ReqLoginDto reqLoginDto) {
        return this.authService.login(reqLoginDto);
    }

    @PostMapping("/register")
    public ResponseEntity<ResRegisterDto> register(@Valid @RequestBody ReqRegisterDto reqRegisterDto) {
        ResRegisterDto response = authService.register(reqRegisterDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/auth/account")
    @ApiMessage("fetch account")
    public ResponseEntity<ResUserJwtDto> getAccount()
    {
        return ResponseEntity.ok().body(this.authService.getAccount());
    }

    @GetMapping("/auth/refresh")
    @ApiMessage("Get user by refresh token")
    public ResponseEntity<ResLoginDto> getRefreshToken(
            @CookieValue(name = "refresh_token", required = false) String refreshToken
    )
    {
        if (refreshToken == null){
            throw new ApiException("Missing refresh token", HttpStatus.UNAUTHORIZED);
        }
        return this.authService.getRefreshToken(refreshToken);
    }

}
