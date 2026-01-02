package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.response.ResUserDto;
import com.cinema.ticketbooking.domain.response.ResUserJwtDto;
import com.cinema.ticketbooking.util.error.BadRequestException;
import com.cinema.ticketbooking.util.error.IdInvalidException;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.cinema.ticketbooking.domain.User;
import com.cinema.ticketbooking.domain.request.ReqLoginDto;
import com.cinema.ticketbooking.domain.request.ReqRegisterDto;
import com.cinema.ticketbooking.domain.response.ResLoginDto;
import com.cinema.ticketbooking.util.SecurityUtil;
import com.cinema.ticketbooking.util.error.DuplicateEmailException;

import java.time.Instant;
import java.util.Random;

@Service
public class AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityUtil securityUtil;
    private final EmailService emailService;

    @Value("${ticketbooking.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    // Inner class to wrap login result with refresh token
    @Data
    @AllArgsConstructor
    public static class LoginResult {
        private ResLoginDto response;
        private String refreshToken;
    }

    AuthService(UserService userService, PasswordEncoder passwordEncoder,
            AuthenticationManagerBuilder authenticationManagerBuilder, SecurityUtil securityUtil,
            EmailService emailService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.securityUtil = securityUtil;
        this.emailService = emailService;
    }

    public LoginResult login(ReqLoginDto reqLoginDto) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(reqLoginDto.getEmail(),
                reqLoginDto.getPassword());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authToken);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        ResLoginDto response = new ResLoginDto();
        User currentUserDB = this.userService.getUserByEmail(reqLoginDto.getEmail());
        ResUserJwtDto jwtUser = null;
        if (currentUserDB != null) {
            jwtUser = new ResUserJwtDto(currentUserDB.getId(), currentUserDB.getUsername(), currentUserDB.getEmail(),
                    currentUserDB.getPhone(), currentUserDB.getGender(), currentUserDB.getAvatar(),
                    currentUserDB.getRole());
            response.setUser(jwtUser);
        }

        // create access token
        String access_token = securityUtil.createAccessToken(authentication.getName(), response);
        response.setAccessToken(access_token);

        // create refresh token
        String refreshToken = this.securityUtil.createRefreshToken(currentUserDB.getEmail(), response);

        // update user
        this.userService.updateUserToken(refreshToken, reqLoginDto.getEmail());

        return new LoginResult(response, refreshToken);
    }

    public ResUserDto register(ReqRegisterDto reqRegisterDto) {
        String email = reqRegisterDto.getEmail().trim();
        if (userService.existsByEmail(email)) {
            throw new DuplicateEmailException("Email existed in system");
        }

        String hashPassword = passwordEncoder.encode(reqRegisterDto.getPassword());
        User registerUser = User.builder()
                .username(reqRegisterDto.getUsername())
                .email(reqRegisterDto.getEmail())
                .password(hashPassword)
                .role(reqRegisterDto.getRole())
                .phone(reqRegisterDto.getPhone())
                .build();

        userService.registerUser(registerUser);
        ResUserDto response = new ResUserDto(registerUser.getId(), registerUser.getUsername(),
                registerUser.getEmail(), registerUser.getPhone(), registerUser.getRole(),
                registerUser.getCreatedAt(), null);

        return response;
    }

    public ResUserJwtDto getAccount() {
        String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : "";

        ResUserJwtDto jwtUser = null;
        User currentUserDB = this.userService.getUserByEmail(email);
        if (currentUserDB != null) {
            jwtUser = new ResUserJwtDto(currentUserDB.getId(), currentUserDB.getUsername(), currentUserDB.getEmail(),
                    currentUserDB.getPhone(), currentUserDB.getGender(), currentUserDB.getAvatar(),
                    currentUserDB.getRole());
        }

        return jwtUser;
    }

    public ResponseEntity<ResLoginDto> getRefreshToken(String refreshToken) {
        // check valid
        Jwt decodedToken = this.securityUtil.checkValidRefreshToken(refreshToken);
        String email = decodedToken.getSubject();

        // check user by token + email
        User currentUser = this.userService.getUserByRefreshTokenAndEmail(refreshToken, email);

        if (currentUser == null) {
            throw new IdInvalidException("Refresh token is invalid");
        }

        // issue new token/set refresh token as cookies
        ResLoginDto response = new ResLoginDto();
        User currentUserDB = this.userService.getUserByEmail(email);
        ResUserJwtDto jwtUser = null;
        if (currentUserDB != null) {
            jwtUser = new ResUserJwtDto(currentUserDB.getId(), currentUserDB.getUsername(), currentUserDB.getEmail(),
                    currentUserDB.getPhone(), currentUserDB.getGender(), currentUserDB.getAvatar(),
                    currentUserDB.getRole());
            response.setUser(jwtUser);
        }

        // create access token
        String access_token = securityUtil.createAccessToken(email, response);
        response.setAccessToken(access_token);

        // create refresh token
        String new_refreshToken = this.securityUtil.createRefreshToken(email, response);

        // update user
        this.userService.updateUserToken(new_refreshToken, email);

        // set cookies
        ResponseCookie resCookie = ResponseCookie.from("refresh_token", new_refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, resCookie.toString())
                .body(response);
    }

    public void forgotPassword(String email) {
        User user = this.userService.getUserByEmail(email);
        if (user == null) {
            throw new BadRequestException("Email does not exist in system");
        }

        // Generate 6-digit OTP
        String otp = String.format("%06d", new Random().nextInt(999999));

        // Set OTP expiration to 5 minutes from now
        Instant otpExpiration = Instant.now().plusSeconds(300);

        // Save OTP to user
        user.setOtpCode(otp);
        user.setOtpExpiration(otpExpiration);
        this.userService.saveUser(user);

        // Send OTP via email
        this.emailService.sendOtpEmail(email, otp);
    }

    public String verifyOtp(String email, String otp) {
        User user = this.userService.getUserByEmail(email);
        if (user == null) {
            throw new BadRequestException("Email does not exist in system");
        }

        if (user.getOtpCode() == null || user.getOtpExpiration() == null) {
            throw new BadRequestException("No OTP found for this email. Please request a new OTP");
        }

        // Check if OTP is expired
        if (Instant.now().isAfter(user.getOtpExpiration())) {
            throw new BadRequestException("OTP has expired. Please request a new OTP");
        }

        // Check if OTP matches
        if (!user.getOtpCode().equals(otp)) {
            throw new BadRequestException("Invalid OTP");
        }

        // Generate reset token
        String resetToken = securityUtil.createResetToken(email);

        // Save reset token to user
        user.setResetToken(resetToken);

        // Clear OTP after verification
        user.setOtpCode(null);
        user.setOtpExpiration(null);

        this.userService.saveUser(user);

        return resetToken;
    }

    public void resetPassword(String resetToken, String newPassword) {
        // Find user by reset token
        User user = this.userService.getUserByResetToken(resetToken);

        if (user == null) {
            throw new BadRequestException("Invalid or expired reset token");
        }

        // Update password
        String hashPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashPassword);

        // Clear reset token
        user.setResetToken(null);

        this.userService.saveUser(user);
    }
}
