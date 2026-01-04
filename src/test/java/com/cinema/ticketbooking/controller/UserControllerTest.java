package com.cinema.ticketbooking.controller;

import com.cinema.ticketbooking.domain.User;
import com.cinema.ticketbooking.domain.request.ReqChangePasswordDto;
import com.cinema.ticketbooking.domain.request.ReqCreateUserDto;
import com.cinema.ticketbooking.domain.request.ReqUpdateUserDto;
import com.cinema.ticketbooking.domain.response.ResUpdateUserDto;
import com.cinema.ticketbooking.domain.response.ResUserDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.service.UserService;
import com.cinema.ticketbooking.util.error.IdInvalidException;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock private UserService userService;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserController userController;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // -----------------------
    // GET /users/{id}
    // -----------------------
    @Test
    void getUserById_shouldReturnOk_whenFound() throws Exception {
        // Arrange
        User u = new User();
        u.setId(1L);

        ResUserDto dto = new ResUserDto();
        dto.setId(1L);

        when(userService.getUserById(1L)).thenReturn(u);
        when(userService.convertToResUserDTO(u)).thenReturn(dto);

        // Act
        ResponseEntity<ResUserDto> res = userController.getUserById(1L);

        // Assert
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertSame(dto, res.getBody());
        verify(userService).getUserById(1L);
        verify(userService).convertToResUserDTO(u);
    }

    @Test
    void getUserById_shouldThrowIdInvalidException_whenNotFound() {
        // Arrange
        when(userService.getUserById(99L)).thenReturn(null);

        // Act + Assert
        IdInvalidException ex = assertThrows(IdInvalidException.class,
                () -> userController.getUserById(99L));
        assertEquals("User with id 99 not found", ex.getMessage());

        verify(userService, never()).convertToResUserDTO(any());
    }

    // -----------------------
    // GET /users
    // -----------------------
    @Test
    void getAllUsers_shouldReturnOkAndBody() {
        // Arrange
        var auth = new UsernamePasswordAuthenticationToken(
                "admin@gmail.com",
                "N/A",
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        Specification<User> spec = null;
        Pageable pageable = PageRequest.of(0, 5);

        ResultPaginationDto dto = new ResultPaginationDto();
        when(userService.getAllUser(spec, pageable)).thenReturn(dto);

        // Act
        ResponseEntity<ResultPaginationDto> res = userController.getAllUsers(spec, pageable);

        // Assert
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertSame(dto, res.getBody());
        verify(userService).getAllUser(spec, pageable);
    }

    // -----------------------
    // POST /users
    // -----------------------
    @Test
    void createUser_shouldThrowException_whenEmailAlreadyExists() {
        // Arrange
        ReqCreateUserDto req = new ReqCreateUserDto();
        req.setEmail("a@gmail.com");
        req.setPassword("plain");

        when(userService.existsByEmail("a@gmail.com")).thenReturn(true);

        // Act + Assert
        Exception ex = assertThrows(Exception.class,
                () -> userController.createUser(req));
        assertEquals("User with email a@gmail.com already exists", ex.getMessage());

        verify(passwordEncoder, never()).encode(anyString());
        verify(userService, never()).createUser(any());
    }

    @Test
    void createUser_shouldEncodePasswordAndReturnCreated_whenValid() throws Exception {
        // Arrange
        ReqCreateUserDto req = new ReqCreateUserDto();
        req.setEmail("a@gmail.com");
        req.setPassword("plain");

        when(userService.existsByEmail("a@gmail.com")).thenReturn(false);
        when(passwordEncoder.encode("plain")).thenReturn("HASHED");

        User saved = new User();
        saved.setId(1L);

        when(userService.createUser(any(ReqCreateUserDto.class))).thenReturn(saved);

        // Act
        ResponseEntity<User> res = userController.createUser(req);

        // Assert
        assertEquals(HttpStatus.CREATED, res.getStatusCode());
        assertSame(saved, res.getBody());

        assertEquals("HASHED", req.getPassword());
        verify(passwordEncoder).encode("plain");
        verify(userService).createUser(req);
    }

    // -----------------------
    // PUT /users
    // -----------------------
    @Test
    void updateUser_shouldThrowIdInvalidException_whenUserNotFound() {
        // Arrange
        ReqUpdateUserDto req = new ReqUpdateUserDto();
        req.setId(10L);

        when(userService.getUserById(10L)).thenReturn(null);

        // Act + Assert
        IdInvalidException ex = assertThrows(IdInvalidException.class,
                () -> userController.updateUser(req));
        assertEquals("User with id 10 does not exist", ex.getMessage());

        verify(userService, never()).updateUser(any());
    }

    @Test
    void updateUser_shouldReturnOk_whenUserExists() {
        // Arrange
        ReqUpdateUserDto req = new ReqUpdateUserDto();
        req.setId(10L);

        when(userService.getUserById(10L)).thenReturn(new User());

        ResUpdateUserDto updated = new ResUpdateUserDto();
        when(userService.updateUser(req)).thenReturn(updated);

        // Act
        ResponseEntity<ResUpdateUserDto> res = userController.updateUser(req);

        // Assert
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertSame(updated, res.getBody());
        verify(userService).updateUser(req);
    }

    // -----------------------
    // PUT /users/me/password
    // -----------------------
    @Test
    void updateMyPassword_shouldCallServiceAndReturnOk_whenValid() {
        // Arrange
        var auth = new UsernamePasswordAuthenticationToken(
                "user@gmail.com",
                "N/A",
                List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER"))
        );
        SecurityContextHolder.getContext().setAuthentication(auth);

        ReqChangePasswordDto request = new ReqChangePasswordDto();
        request.setCurrentPassword("oldpass");
        request.setNewPassword("newpass");

        // Act
        ResponseEntity<Void> res = userController.updateMyPassword(request);

        // Assert
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertNull(res.getBody());
        verify(userService).updateMyPassword("user@gmail.com", "oldpass", "newpass");
    }

    // -----------------------
    // DELETE /users/{id}
    // -----------------------
    @Test
    void deleteUser_shouldThrowIdInvalidException_whenNotFound() {
        // Arrange
        when(userService.getUserById(5L)).thenReturn(null);

        // Act + Assert
        IdInvalidException ex = assertThrows(IdInvalidException.class,
                () -> userController.deleteUser(5L));
        assertEquals("User with id 5 does not exist", ex.getMessage());

        verify(userService, never()).deleteUser(anyLong());
    }

    @Test
    void deleteUser_shouldReturnOk_whenFound() {
        // Arrange
        when(userService.getUserById(5L)).thenReturn(new User());

        // Act
        ResponseEntity<Void> res = userController.deleteUser(5L);

        // Assert
        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertNull(res.getBody());
        verify(userService).deleteUser(5L);
    }
}
