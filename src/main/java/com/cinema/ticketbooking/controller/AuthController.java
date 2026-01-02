package com.cinema.ticketbooking.controller;

import com.cinema.ticketbooking.domain.request.ReqForgotPasswordDto;
import com.cinema.ticketbooking.domain.request.ReqResetPasswordDto;
import com.cinema.ticketbooking.domain.request.ReqVerifyOtpDto;
import com.cinema.ticketbooking.domain.response.ResUserDto;
import com.cinema.ticketbooking.domain.response.ResUserJwtDto;
import com.cinema.ticketbooking.domain.response.ResVerifyOtpDto;
import com.cinema.ticketbooking.service.UserService;
import com.cinema.ticketbooking.util.SecurityUtil;
import com.cinema.ticketbooking.util.annotation.ApiMessage;
import com.cinema.ticketbooking.util.error.ApiException;
import com.cinema.ticketbooking.util.error.IdInvalidException;
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
import com.cinema.ticketbooking.service.AuthService;

@RestController
@RequestMapping("api/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @Value("${ticketbooking.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    @PostMapping("/login")
    public ResponseEntity<ResLoginDto> login(@Valid @RequestBody ReqLoginDto reqLoginDto) {
        var loginResult = this.authService.login(reqLoginDto);
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", loginResult.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(loginResult.getResponse());
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

    @PostMapping("/logout")
    @ApiMessage("Logout User")
    public ResponseEntity<Void> logout() {
        String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : "";

        if (email.equals("")) {
            throw new IdInvalidException("Access Token is invalid");
        }

        // update refresh token = null
        this.userService.updateUserToken(null, email);

        // remove refresh token cookie
        ResponseCookie deleteSpringCookie = ResponseCookie
                .from("refresh_token", null)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteSpringCookie.toString()).build();
    }

    @PostMapping("/forgot-password")
    @ApiMessage("Send OTP to email for password reset")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ReqForgotPasswordDto request) {
        this.authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verify-otp")
    @ApiMessage("Verify OTP and get reset token")
    public ResponseEntity<ResVerifyOtpDto> verifyOtp(@Valid @RequestBody ReqVerifyOtpDto request) {
        String resetToken = this.authService.verifyOtp(request.getEmail(), request.getOtp());
        ResVerifyOtpDto response = new ResVerifyOtpDto();
        response.setResetToken(resetToken);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/reset-password")
    @ApiMessage("Reset password with reset token")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ReqResetPasswordDto request) {
        this.authService.resetPassword(request.getResetToken(), request.getNewPassword());
        return ResponseEntity.ok().build();
    }

}
