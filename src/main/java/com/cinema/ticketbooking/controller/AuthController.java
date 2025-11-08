package com.cinema.ticketbooking.controller;

import com.cinema.ticketbooking.domain.RestResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.cinema.ticketbooking.domain.dto.LoginDto;
import com.cinema.ticketbooking.domain.dto.ResLoginDto;
import com.cinema.ticketbooking.util.SecurityUtil;

@RestController
public class AuthController {
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final SecurityUtil securityUtil;

    public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder, SecurityUtil securityUtil) {
        this.authenticationManagerBuilder = authenticationManagerBuilder;
        this.securityUtil = securityUtil;
    }


    @PostMapping("/login")
    public ResponseEntity<ResLoginDto> Login(@Valid @RequestBody LoginDto loginDto) {
        //Nạp input gồm username/password vào Security
        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword());

        //xác thực người dùng => cần viết hàm loadUserByUsername
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        //create a token
        String access_token = this.securityUtil.createToken(authentication);
        SecurityContextHolder.getContext().setAuthentication(authentication);


        ResLoginDto res = new ResLoginDto();
        res.setAccessToken(access_token);
        return ResponseEntity.ok().body(res);

    }


}
