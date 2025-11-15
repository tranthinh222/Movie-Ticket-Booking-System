package com.cinema.ticketbooking.service;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.cinema.ticketbooking.domain.User;
import com.cinema.ticketbooking.domain.dto.LoginDto;
import com.cinema.ticketbooking.domain.dto.RegisterDto;
import com.cinema.ticketbooking.domain.dto.ResLoginDto;
import com.cinema.ticketbooking.domain.dto.ResRegisterDto;
import com.cinema.ticketbooking.util.SecurityUtil;
import com.cinema.ticketbooking.util.error.DuplicateEmailException;

@Service
public class AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityUtil securityUtil;

    AuthService(UserService userService, PasswordEncoder passwordEncoder,
            AuthenticationManagerBuilder authenticationManagerBuilder, SecurityUtil securityUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.securityUtil = securityUtil;
    }

    public ResLoginDto login(LoginDto loginDto) {
        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(loginDto.getEmail(),
                loginDto.getPassword());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authToken);

        String access_token = securityUtil.createToken(authentication);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        ResLoginDto response = new ResLoginDto();

        response.setAccessToken(access_token);
        response.setEmail(loginDto.getEmail());
        return response;
    }

    public ResRegisterDto register(RegisterDto registerDto) {
        String email = registerDto.getEmail().trim();
        if (userService.existsByEmail(email)) {
            throw new DuplicateEmailException("Email existed in system");
        }

        String hashPassword = passwordEncoder.encode(registerDto.getPassword());
        User registerUser = User.builder()
                .username(registerDto.getUsername())
                .email(registerDto.getEmail())
                .password(hashPassword)
                .role(registerDto.getRole())
                .phone(registerDto.getPhone())
                .build();

        userService.createUser(registerUser);

        // Đăng nhập luôn tăng UX

        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(registerDto.getEmail(),
                registerDto.getPassword());
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authToken);

        String access_token = securityUtil.createToken(authentication);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        ResRegisterDto response = new ResRegisterDto();

        response.setAccessToken(access_token);
        response.setEmail(registerUser.getEmail());
        return response;
    }
}
