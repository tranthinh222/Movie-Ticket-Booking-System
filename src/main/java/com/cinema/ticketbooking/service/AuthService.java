package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.response.ResUserJwtDto;
import com.cinema.ticketbooking.util.error.IdInvalidException;
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
import com.cinema.ticketbooking.domain.response.ResUserDto;
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

    public ResLoginDto login(ReqLoginDto reqLoginDto) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(reqLoginDto.getEmail(),
                reqLoginDto.getPassword());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authToken);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        ResLoginDto response = new ResLoginDto();
        User currentUserDB = this.userService.getUserByEmail(reqLoginDto.getEmail());
        ResUserJwtDto jwtUser = null;
        if (currentUserDB != null) {
            jwtUser = new ResUserJwtDto(currentUserDB.getId(), currentUserDB.getUsername(), currentUserDB.getEmail());
            response.setUser(jwtUser);
        }

        // create access token
        String access_token = securityUtil.createAccessToken(authentication.getName(), response);
        response.setAccessToken(access_token);

        // create refresh token
        String refreshToken = this.securityUtil.createRefreshToken(currentUserDB.getEmail(), response);
        response.setRefreshToken(refreshToken);

        // update user
        this.userService.updateUserToken(refreshToken, reqLoginDto.getEmail());

        // set cookies
        return response;
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
                .createdAt(reqRegisterDto.getCreatedAt())
                .build();

        userService.createUser(registerUser);
        ResUserDto response = new ResUserDto(registerUser.getId(), registerUser.getEmail(),
                registerUser.getUsername(), registerUser.getPhone(), registerUser.getRole(),
                registerUser.getCreatedAt(), null);

        return response;
    }

    public ResUserJwtDto getAccount() {
        String email = SecurityUtil.getCurrentUserLogin().isPresent() ? SecurityUtil.getCurrentUserLogin().get() : "";

        ResUserJwtDto jwtUser = null;
        User currentUserDB = this.userService.getUserByEmail(email);
        if (currentUserDB != null) {
            jwtUser = new ResUserJwtDto(currentUserDB.getId(), currentUserDB.getUsername(), currentUserDB.getEmail());
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
            jwtUser = new ResUserJwtDto(currentUserDB.getId(), currentUserDB.getUsername(), currentUserDB.getEmail());
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
        ResponseCookie resCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, resCookie.toString())
                .body(response);
    }

}
