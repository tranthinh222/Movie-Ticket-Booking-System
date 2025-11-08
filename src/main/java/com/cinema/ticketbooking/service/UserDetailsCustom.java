package com.cinema.ticketbooking.service;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;


@Component("userDetailsService")
public class UserDetailsCustom implements UserDetailsService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public UserDetailsCustom(UserService  userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException{
        com.cinema.ticketbooking.domain.User user = this.userService.getUserByEmail(email);
        if (user == null){
            throw new UsernameNotFoundException("Username/password invalid");
        }
        return new User(
                user.getEmail(),
                user.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }


}
