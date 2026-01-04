package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.User;
import com.cinema.ticketbooking.domain.request.ReqCreateUserDto;
import com.cinema.ticketbooking.domain.request.ReqUpdateUserDto;
import com.cinema.ticketbooking.domain.response.ResUpdateUserDto;
import com.cinema.ticketbooking.domain.response.ResUserDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.repository.UserRepository;
import com.cinema.ticketbooking.util.constant.RoleEnum;
import com.cinema.ticketbooking.util.error.BadRequestException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserService userService;

    // -----------------------
    // getUserById
    // -----------------------
    @Test
    void getUserById_shouldReturnUser_whenExists() {
        User u = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));

        User result = userService.getUserById(1L);

        assertSame(u, result);
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_shouldReturnNull_whenNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        User result = userService.getUserById(1L);

        assertNull(result);
        verify(userRepository).findById(1L);
    }

    // -----------------------
    // getAllUser (pagination + remove sensitive data)
    // -----------------------
    @Test
    void getAllUser_shouldReturnMetaAndResUserDtoList() {
        // Arrange
        Specification<User> spec = null;
        Pageable pageable = PageRequest.of(2, 5);

        RoleEnum role0 = RoleEnum.values()[0];
        RoleEnum role1 = RoleEnum.values().length > 1 ? RoleEnum.values()[1] : RoleEnum.values()[0];

        User u1 = new User();
        u1.setId(1);
        u1.setUsername("a");
        u1.setEmail("a@gmail.com");
        u1.setPhone("111");
        u1.setRole(role0);
        u1.setPassword("SECRET1");

        User u2 = new User();
        u2.setId(2);
        u2.setUsername("b");
        u2.setEmail("b@gmail.com");
        u2.setPhone("222");
        u2.setRole(role1);
        u2.setPassword("SECRET2");

        List<User> content = List.of(u1, u2);
        Page<User> page = new PageImpl<>(content, pageable, 12);

        when(userRepository.findAll(spec, pageable)).thenReturn(page);

        // Act
        ResultPaginationDto rs = userService.getAllUser(spec, pageable);

        // Assert meta theo code hiện tại
        assertNotNull(rs.getMeta());
        assertEquals(6, rs.getMeta().getPageSize());
        assertEquals(12, rs.getMeta().getTotalItems());
        assertEquals(3, rs.getMeta().getTotalPages());
        assertEquals(2, rs.getMeta().getCurrentPage());

        // data là List<ResUserDto>
        List<?> data = (List<?>) rs.getData();
        assertEquals(2, data.size());
        assertTrue(data.get(0) instanceof ResUserDto);

        ResUserDto d1 = (ResUserDto) data.get(0);
        assertEquals(1L, d1.getId());
        assertEquals("a", d1.getUsername());
        assertEquals("a@gmail.com", d1.getEmail());
        assertEquals("111", d1.getPhone());
        assertEquals(role0, d1.getRole());

        ResUserDto d2 = (ResUserDto) data.get(1);
        assertEquals(2L, d2.getId());
        assertEquals(role1, d2.getRole());

        verify(userRepository).findAll(spec, pageable);
    }


    // -----------------------
    // createUser
    // -----------------------
    @Test
    void createUser_shouldMapFieldsAndSave() {
        // Arrange
        ReqCreateUserDto req = new ReqCreateUserDto();
        req.setUsername("minhquan");
        req.setEmail("mq@gmail.com");
        req.setPhone("0909");
        req.setPassword("plain");
        req.setRole(RoleEnum.values()[0]);

        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        // Act
        User saved = userService.createUser(req);

        // Assert
        verify(userRepository).save(captor.capture());
        User arg = captor.getValue();

        assertEquals(req.getUsername(), arg.getUsername());
        assertEquals(req.getEmail(), arg.getEmail());
        assertEquals(req.getPhone(), arg.getPhone());
        assertEquals(req.getPassword(), arg.getPassword());
        assertEquals(req.getRole(), arg.getRole());

        assertSame(saved, arg);
    }


    // -----------------------
    // registerUser
    // -----------------------
    @Test
    void registerUser_shouldSaveUser() {
        User u = new User();
        when(userRepository.save(u)).thenReturn(u);

        User result = userService.registerUser(u);

        assertSame(u, result);
        verify(userRepository).save(u);
    }

    // -----------------------
    // convertToResUserDTO
    // -----------------------
    @Test
    void convertToResUserDTO_shouldCopyNonSensitiveFields() {
        User u = new User();
        u.setId(1);
        u.setUsername("a");
        u.setEmail("a@gmail.com");
        u.setPhone("111");
        u.setRole(RoleEnum.values()[0]);

        Instant now = Instant.now();
        u.setCreatedAt(now);
        u.setUpdatedAt(now);

        ResUserDto dto = userService.convertToResUserDTO(u);

        assertEquals(1L, dto.getId());
        assertEquals("a", dto.getUsername());
        assertEquals("a@gmail.com", dto.getEmail());
        assertEquals("111", dto.getPhone());
        assertEquals(u.getRole(), dto.getRole());
        assertEquals(now, dto.getCreatedAt());
        assertEquals(now, dto.getUpdatedAt());
    }


    // -----------------------
    // getUserByEmail / existsByEmail
    // -----------------------
    @Test
    void getUserByEmail_shouldCallRepository() {
        User u = new User();
        when(userRepository.findUserByEmail("x@gmail.com")).thenReturn(u);

        User result = userService.getUserByEmail("x@gmail.com");

        assertSame(u, result);
        verify(userRepository).findUserByEmail("x@gmail.com");
    }

    @Test
    void existsByEmail_shouldReturnFromRepository() {
        when(userRepository.existsByEmail("a@gmail.com")).thenReturn(true);

        assertTrue(userService.existsByEmail("a@gmail.com"));
        verify(userRepository).existsByEmail("a@gmail.com");
    }

    // -----------------------
    // updateUser
    // -----------------------
    @Test
    void updateUser_shouldReturnNull_whenUserNotFound() {
        ReqUpdateUserDto req = new ReqUpdateUserDto();
        req.setId(99L);

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        ResUpdateUserDto result = userService.updateUser(req);

        assertNull(result);
        verify(userRepository).findById(99L);
    }

    @Test
    void updateUser_shouldReturnResUpdateUserDto_whenUserFound() {
        ReqUpdateUserDto req = new ReqUpdateUserDto();
        req.setId(1L);
        req.setUsername("abc");
        req.setPhone("0909");

        User u = new User();
        u.setUsername("abc");
        u.setPhone("0909");

        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(userRepository.save(any(User.class))).thenReturn(u);

        ResUpdateUserDto result = userService.updateUser(req);

        assertNotNull(result);
        assertEquals("abc", result.getUsername());
        assertEquals("0909", result.getPhone());

        verify(userRepository).save(any(User.class));
    }

    // -----------------------
    // deleteUser
    // -----------------------
    @Test
    void deleteUser_shouldCallRepositoryDeleteById() {
        userService.deleteUser(10L);
        verify(userRepository).deleteById(10L);
    }

    // -----------------------
    // updateUserToken
    // -----------------------
    @Test
    void updateUserToken_shouldSetRefreshTokenAndSave_whenUserExists() {
        User u = new User();
        when(userRepository.findUserByEmail("a@gmail.com")).thenReturn(u);

        userService.updateUserToken("token123", "a@gmail.com");

        assertEquals("token123", u.getRefreshToken());
        verify(userRepository).save(u);
    }

    @Test
    void updateUserToken_shouldDoNothing_whenUserNotFound() {
        when(userRepository.findUserByEmail("missing@gmail.com")).thenReturn(null);

        userService.updateUserToken("token123", "missing@gmail.com");

        verify(userRepository, never()).save(any());
    }

    // -----------------------
    // updateMyPassword
    // -----------------------
    @Test
    void updateMyPassword_shouldEncodePasswordAndSave_whenOldPasswordCorrect() {
        User u = new User();
        u.setPassword("OLD_HASHED");
        when(userRepository.findUserByEmail("a@gmail.com")).thenReturn(u);
        when(passwordEncoder.matches("oldpass", "OLD_HASHED")).thenReturn(true);
        when(passwordEncoder.encode("newpass")).thenReturn("NEW_HASHED");

        userService.updateMyPassword("a@gmail.com", "oldpass", "newpass");

        assertEquals("NEW_HASHED", u.getPassword());
        verify(userRepository).save(u);
        verify(passwordEncoder).encode("newpass");
    }

    // -----------------------
    // getUserByRefreshTokenAndEmail
    // -----------------------
    @Test
    void getUserByRefreshTokenAndEmail_shouldCallRepository() {
        User u = new User();
        when(userRepository.findByRefreshTokenAndEmail("rt", "a@gmail.com")).thenReturn(u);

        User result = userService.getUserByRefreshTokenAndEmail("rt", "a@gmail.com");

        assertSame(u, result);
        verify(userRepository).findByRefreshTokenAndEmail("rt", "a@gmail.com");
    }
}
