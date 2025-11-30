package com.cinema.ticketbooking.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cinema.ticketbooking.domain.User;
import com.cinema.ticketbooking.domain.request.ReqLoginDto;
import com.cinema.ticketbooking.domain.request.ReqRegisterDto;
import com.cinema.ticketbooking.domain.response.ResLoginDto;
import com.cinema.ticketbooking.domain.response.ResRegisterDto;
import com.cinema.ticketbooking.util.SecurityUtil;
import com.cinema.ticketbooking.util.error.DuplicateEmailException;

@Service
public class AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityUtil securityUtil;
    @Value("${ticketbooking.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    AuthService(UserService userService, PasswordEncoder passwordEncoder,
            AuthenticationManagerBuilder authenticationManagerBuilder, SecurityUtil securityUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.securityUtil = securityUtil;
    }

    public ResponseEntity<ResLoginDto> login(ReqLoginDto reqLoginDto) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(reqLoginDto.getEmail(),
                reqLoginDto.getPassword());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authToken);

        String access_token = securityUtil.createAccessToken(authentication);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        ResLoginDto response = new ResLoginDto();
        User currentUserDB = this.userService.getUserByEmail(reqLoginDto.getEmail());
        if (currentUserDB != null) {
            ResLoginDto.UserLogin userLogin = new ResLoginDto.UserLogin(
                    currentUserDB.getId(),
                    currentUserDB.getUsername(),
                    currentUserDB.getEmail()
            );

            response.setUser(userLogin);
        }

        response.setAccessToken(access_token);

        //create refresh token
        String refreshToken = this.securityUtil.createRefreshToken(currentUserDB.getEmail(), response);

        //update user
        this.userService.updateUserToken(refreshToken, reqLoginDto.getEmail());

        //set cookies
        ResponseCookie resCookie = ResponseCookie.from("refresh_token",    refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, resCookie.toString())
                .body(response);
    }

    public ResRegisterDto register(ReqRegisterDto reqRegisterDto) {
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

        userService.createUser(registerUser);

        // Đăng nhập luôn tăng UX

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(reqRegisterDto.getEmail(),
                reqRegisterDto.getPassword());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authToken);

        String access_token = securityUtil.createAccessToken(authentication);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        ResRegisterDto response = new ResRegisterDto();

        response.setAccessToken(access_token);
        response.setEmail(registerUser.getEmail());
        return response;
    }
}
