package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.User;
import com.cinema.ticketbooking.domain.request.ReqCreateUserDto;
import com.cinema.ticketbooking.domain.request.ReqUpdateUserDto;
import com.cinema.ticketbooking.domain.response.ResUpdateUserDto;
import com.cinema.ticketbooking.domain.response.ResUserDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.repository.UserRepository;
import com.cinema.ticketbooking.util.error.BadRequestException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User getUserById(long id) {
        return this.userRepository.findById(id).orElse(null);
    }

    public ResultPaginationDto getAllUser(Specification<User> spec, Pageable pageable) {
        Page<User> pageUser = this.userRepository.findAll(spec, pageable);
        ResultPaginationDto rs = new ResultPaginationDto();

        ResultPaginationDto.Meta meta = new ResultPaginationDto.Meta();
        meta.setPageSize(pageable.getPageSize() + 1);
        meta.setTotalItems(pageUser.getTotalElements());
        meta.setTotalPages(pageUser.getTotalPages());
        meta.setCurrentPage(pageable.getPageNumber());

        rs.setMeta(meta);
        rs.setData(pageUser.getContent());

        // remove sensitive data
        List<ResUserDto> listUser = pageUser.stream().map(item -> new ResUserDto(
                item.getId(),
                item.getUsername(),
                item.getEmail(),
                item.getPhone(),
                item.getRole(),
                item.getCreatedAt(),
                item.getUpdatedAt())).collect(Collectors.toList());

        rs.setData(listUser);
        return rs;
    }

    public User createUser(ReqCreateUserDto reqUser) {
        User user = new User();
        user.setUsername(reqUser.getUsername());
        user.setEmail(reqUser.getEmail());
        user.setPhone(reqUser.getPhone());
        user.setPassword(reqUser.getPassword());
        user.setRole(reqUser.getRole());
        return this.userRepository.save(user);
    }

    public User registerUser(User user) {
        return this.userRepository.save(user);
    }

    public ResUserDto convertToResUserDTO(User user) {
        ResUserDto resUserDto = new ResUserDto();
        resUserDto.setId(user.getId());
        resUserDto.setUsername(user.getUsername());
        resUserDto.setEmail(user.getEmail());
        resUserDto.setPhone(user.getPhone());
        resUserDto.setRole(user.getRole());
        resUserDto.setCreatedAt(user.getCreatedAt());
        resUserDto.setUpdatedAt(user.getUpdatedAt());
        return resUserDto;
    }

    public User getUserByEmail(String email) {
        return this.userRepository.findUserByEmail(email);
    }

    public boolean existsByEmail(String email) {
        return this.userRepository.existsByEmail(email);
    }

    public ResUpdateUserDto updateUser(ReqUpdateUserDto reqUser) {
        Optional<User> userOptional = this.userRepository.findById(reqUser.getId());
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Debug log
            System.out.println("Before update - Phone: " + user.getPhone());
            System.out.println("Request Phone: " + reqUser.getPhone());

            // Cập nhật thông tin mới
            user.setUsername(reqUser.getUsername());
            user.setPhone(reqUser.getPhone());
            user.setGender(reqUser.getGender());
            user.setAvatar(reqUser.getAvatar());

            System.out.println("After setPhone - Phone: " + user.getPhone());

            // Lưu vào database
            User savedUser = this.userRepository.save(user);

            System.out.println("After save - Phone: " + savedUser.getPhone());

            // Trả về response
            ResUpdateUserDto updatedUser = new ResUpdateUserDto();
            updatedUser.setUsername(savedUser.getUsername());
            updatedUser.setPhone(savedUser.getPhone());
            updatedUser.setGender(savedUser.getGender());
            updatedUser.setAvatar(savedUser.getAvatar());

            return updatedUser;
        }

        return null;
    }

    public void deleteUser(long id) {
        this.userRepository.deleteById(id);
    }

    public void updateUserToken(String token, String email) {
        User currentUser = this.getUserByEmail(email);
        if (currentUser != null) {
            currentUser.setRefreshToken(token);
            this.userRepository.save(currentUser);
        }
    }

    public void updateMyPassword(String email, String oldPassword, String newPassword) {
        User user = this.getUserByEmail(email);

        // Debug log
        System.out.println("=== Change Password Debug ===");
        System.out.println("Email: " + email);
        System.out.println("User found: " + (user != null ? user.getUsername() : "null"));
        System.out.println("Old password matches: " + passwordEncoder.matches(oldPassword, user.getPassword()));

        // Verify old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestException("Old password is incorrect");
        }

        // Hash and save new password
        String hashPassword = passwordEncoder.encode(newPassword);
        user.setPassword(hashPassword);
        this.userRepository.save(user);

        System.out.println("Password changed successfully for: " + email);
    }

    public User getUserByRefreshTokenAndEmail(String refreshToken, String email) {
        return this.userRepository.findByRefreshTokenAndEmail(refreshToken, email);
    }

    public User saveUser(User user) {
        return this.userRepository.save(user);
    }

    public User getUserByResetToken(String resetToken) {
        return this.userRepository.findByResetToken(resetToken);
    }

}
