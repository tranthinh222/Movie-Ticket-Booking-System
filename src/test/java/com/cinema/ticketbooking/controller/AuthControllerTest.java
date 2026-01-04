package com.cinema.ticketbooking.controller;

import com.cinema.ticketbooking.domain.request.ReqLoginDto;
import com.cinema.ticketbooking.domain.request.ReqRegisterDto;
import com.cinema.ticketbooking.domain.response.ResLoginDto;
import com.cinema.ticketbooking.domain.response.ResUserDto;
import com.cinema.ticketbooking.domain.response.ResUserJwtDto;
import com.cinema.ticketbooking.service.AuthService;
import com.cinema.ticketbooking.service.AuthService.LoginResult;
import com.cinema.ticketbooking.service.UserService;
import com.cinema.ticketbooking.util.SecurityUtil;
import com.cinema.ticketbooking.util.error.ApiException;
import com.cinema.ticketbooking.util.error.IdInvalidException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthController authController;

    @Test
    void login_shouldReturnResLoginDtoAndSetCookie_whenValidRequest() throws Exception {
        // Arrange
        ReqLoginDto request = new ReqLoginDto();
        request.setEmail("test@test.com");
        request.setPassword("password");

        ResLoginDto responseDto = new ResLoginDto();
        responseDto.setAccessToken("access-token");

        LoginResult loginResult = new LoginResult(responseDto, "refresh-token");

        // Inject private field refreshTokenExpiration using Reflection
        Field field = AuthController.class.getDeclaredField("refreshTokenExpiration");
        field.setAccessible(true);
        field.set(authController, 3600L);

        when(authService.login(request)).thenReturn(loginResult);

        // Act
        ResponseEntity<ResLoginDto> response = authController.login(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDto, response.getBody());
        assertTrue(response.getHeaders().containsKey(HttpHeaders.SET_COOKIE));
        assertTrue(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE).contains("refresh-token"));
        verify(authService).login(request);
    }

    @Test
    void register_shouldReturnResUserDto_whenValidRequest() {
        // Arrange
        ReqRegisterDto request = new ReqRegisterDto();
        ResUserDto responseDto = new ResUserDto();
        when(authService.register(request)).thenReturn(responseDto);

        // Act
        ResponseEntity<ResUserDto> response = authController.register(request);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(responseDto, response.getBody());
        verify(authService).register(request);
    }

    @Test
    void getAccount_shouldReturnResUserJwtDto() {
        // Arrange
        ResUserJwtDto responseDto = new ResUserJwtDto();
        when(authService.getAccount()).thenReturn(responseDto);

        // Act
        ResponseEntity<ResUserJwtDto> response = authController.getAccount();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDto, response.getBody());
        verify(authService).getAccount();
    }

    @Test
    void getRefreshToken_shouldReturnResLoginDto_whenRefreshTokenProvided() {
        // Arrange
        String token = "refresh-token";
        ResLoginDto responseDto = new ResLoginDto();
        when(authService.getRefreshToken(token)).thenReturn(ResponseEntity.ok(responseDto));

        // Act
        ResponseEntity<ResLoginDto> response = authController.getRefreshToken(token);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(responseDto, response.getBody());
        verify(authService).getRefreshToken(token);
    }

    @Test
    void getRefreshToken_shouldThrowApiException_whenTokenMissing() {
        // Act & Assert
        ApiException exception = assertThrows(
                ApiException.class,
                () -> authController.getRefreshToken(null)
        );
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatus());
        assertEquals("Missing refresh token", exception.getMessage());
    }

    @Test
    void logout_shouldReturnOkAndDeleteCookie_whenUserLoggedIn() {
        // Arrange
        try (MockedStatic<SecurityUtil> utilities = mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getCurrentUserLogin).thenReturn(Optional.of("user@example.com"));

            // Act
            ResponseEntity<Void> response = authController.logout();

            // Assert
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertTrue(response.getHeaders().containsKey(HttpHeaders.SET_COOKIE));
            assertTrue(response.getHeaders().getFirst(HttpHeaders.SET_COOKIE).contains("refresh_token="));
            verify(userService).updateUserToken(null, "user@example.com");
        }
    }

    @Test
    void logout_shouldThrowIdInvalidException_whenUserNotLoggedIn() {
        // Arrange
        try (MockedStatic<SecurityUtil> utilities = mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getCurrentUserLogin).thenReturn(Optional.empty());

            // Act & Assert
            IdInvalidException exception = assertThrows(
                    IdInvalidException.class,
                    () -> authController.logout()
            );
            assertEquals("Access Token is invalid", exception.getMessage());
        }
    }
}
