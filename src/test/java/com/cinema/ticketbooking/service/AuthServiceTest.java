package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.User;
import com.cinema.ticketbooking.domain.request.ReqLoginDto;
import com.cinema.ticketbooking.domain.request.ReqRegisterDto;
import com.cinema.ticketbooking.domain.response.ResLoginDto;
import com.cinema.ticketbooking.domain.response.ResUserDto;
import com.cinema.ticketbooking.domain.response.ResUserJwtDto;
import com.cinema.ticketbooking.service.AuthService.LoginResult;
import com.cinema.ticketbooking.util.SecurityUtil;
import com.cinema.ticketbooking.util.constant.RoleEnum;
import com.cinema.ticketbooking.util.error.DuplicateEmailException;
import com.cinema.ticketbooking.util.error.IdInvalidException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManagerBuilder authenticationManagerBuilder;

    @Mock
    private SecurityUtil securityUtil;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        // Set refreshTokenExpiration cho test
        ReflectionTestUtils.setField(authService, "refreshTokenExpiration", 3600L);
    }

    @Test
    void login_shouldReturnLoginResult_whenCredentialsValid() {
        // Arrange
        ReqLoginDto req = new ReqLoginDto();
        req.setEmail("test@test.com"); 
        req.setPassword("password");

        User user = new User();
        user.setEmail("test@test.com");
        user.setUsername("Tester");
        user.setRole(RoleEnum.CUSTOMER);

        when(authentication.getName()).thenReturn("test@test.com");
        when(authenticationManagerBuilder.getObject()).thenReturn(auth -> authentication);
        when(userService.getUserByEmail("test@test.com")).thenReturn(user);
        doReturn("access_token").when(securityUtil).createAccessToken(anyString(), any());
        doReturn("refresh_token").when(securityUtil).createRefreshToken(anyString(), any());

        // Act
        LoginResult result = authService.login(req);

        // Assert
        assertNotNull(result);
        assertEquals("refresh_token", result.getRefreshToken());
        assertNotNull(result.getResponse());
        assertEquals("access_token", result.getResponse().getAccessToken());
        assertNotNull(result.getResponse().getUser());
        assertEquals("Tester", result.getResponse().getUser().getUsername());
        assertEquals(RoleEnum.CUSTOMER, result.getResponse().getUser().getRole());
        verify(userService).updateUserToken("refresh_token", "test@test.com");
    }

    @Test
    void register_shouldReturnResUserDto_whenEmailNotExist() {
        // Arrange
        ReqRegisterDto req = new ReqRegisterDto();
        req.setEmail("new@test.com");
        req.setPassword("pass");
        req.setUsername("NewUser");
        req.setRole(RoleEnum.CUSTOMER);
        req.setPhone("123");

        when(userService.existsByEmail("new@test.com")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("hashed");

        // Act
        ResUserDto result = authService.register(req);

        // Assert
        assertNotNull(result);
        assertEquals("new@test.com", result.getEmail());
        assertEquals("NewUser", result.getUsername());
        verify(userService).registerUser(any(User.class));
    }

    @Test
    void register_shouldThrowDuplicateEmailException_whenEmailExists() {
        // Arrange
        ReqRegisterDto req = new ReqRegisterDto();
        req.setEmail("exist@test.com");

        when(userService.existsByEmail("exist@test.com")).thenReturn(true);

        // Act & Assert
        assertThrows(DuplicateEmailException.class, () -> authService.register(req));
    }

    @Test
    void getAccount_shouldReturnResUserJwtDto_whenUserExists() {
        // Arrange
        User user = new User();
        user.setEmail("a@test.com");
        user.setUsername("UserA");
        user.setId(1L);
        user.setRole(RoleEnum.CUSTOMER);

        try (MockedStatic<SecurityUtil> utilities = mockStatic(SecurityUtil.class)) {
            utilities.when(SecurityUtil::getCurrentUserLogin).thenReturn(Optional.of("a@test.com"));
            when(userService.getUserByEmail("a@test.com")).thenReturn(user);

            // Act
            ResUserJwtDto result = authService.getAccount();

            // Assert
            assertNotNull(result);
            assertEquals("UserA", result.getUsername());
            assertEquals(RoleEnum.CUSTOMER, result.getRole());
        }
    }

    @Test
    void getRefreshToken_shouldReturnResponseEntity_whenTokenValid() {
        // Arrange
        String refreshToken = "refresh_token";
        Jwt jwt = mock(Jwt.class);
        when(securityUtil.checkValidRefreshToken(refreshToken)).thenReturn(jwt);
        when(jwt.getSubject()).thenReturn("a@test.com");

        User user = new User();
        user.setEmail("a@test.com");
        user.setUsername("UserA");

        when(userService.getUserByRefreshTokenAndEmail(refreshToken, "a@test.com")).thenReturn(user);
        when(userService.getUserByEmail("a@test.com")).thenReturn(user);
        doReturn("access_token").when(securityUtil).createAccessToken(anyString(), any());
        doReturn("new_refresh_token").when(securityUtil).createRefreshToken(anyString(), any());

        // Act
        ResponseEntity<ResLoginDto> response = authService.getRefreshToken(refreshToken);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResLoginDto body = response.getBody();
        assertNotNull(body);
        assertEquals("access_token", body.getAccessToken());
        verify(userService).updateUserToken("new_refresh_token", "a@test.com");
    }

    @Test
    void getRefreshToken_shouldThrowIdInvalidException_whenUserNotFound() {
        // Arrange
        String refreshToken = "invalid_token";
        Jwt jwt = mock(Jwt.class);
        when(securityUtil.checkValidRefreshToken(refreshToken)).thenReturn(jwt);
        when(jwt.getSubject()).thenReturn("b@test.com");
        when(userService.getUserByRefreshTokenAndEmail(refreshToken, "b@test.com")).thenReturn(null);

        // Act & Assert
        assertThrows(IdInvalidException.class, () -> authService.getRefreshToken(refreshToken));
    }
}
