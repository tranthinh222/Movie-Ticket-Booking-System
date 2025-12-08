package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.User;
import com.cinema.ticketbooking.domain.request.ReqCreateUserDto;
import com.cinema.ticketbooking.domain.request.ReqUpdateUserDto;
import com.cinema.ticketbooking.domain.response.ResUpdateUserDto;
import com.cinema.ticketbooking.domain.response.ResUserDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User getUserById(long id) {
        return this.userRepository.findById(id).orElse(null);
    }

    public ResultPaginationDto  getAllUser(Specification<User> spec, Pageable pageable) {
        Page<User> pageUser = this.userRepository.findAll(spec, pageable);
        ResultPaginationDto rs = new ResultPaginationDto();

        ResultPaginationDto.Meta meta = new ResultPaginationDto.Meta();
        meta.setPageSize(pageable.getPageSize() + 1);
        meta.setTotalItems(pageUser.getTotalElements());
        meta.setTotalPages(pageUser.getTotalPages());
        meta.setCurrentPage(pageable.getPageNumber());

        rs.setMeta(meta);
        rs.setData(pageUser.getContent());

        //remove sensitive data
        List<ResUserDto> listUser = pageUser.stream().map(item -> new ResUserDto(
                item.getId(),
                item.getUsername(),
                item.getEmail(),
                item.getPhone(),
                item.getRole(),
                item.getCreatedAt(),
                item.getUpdatedAt()
        )).collect(Collectors.toList());

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

    public User registerUser(User user){
        return this.userRepository.save(user);
    }

    public ResUserDto convertToResUserDTO(User user){
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

    public ResUpdateUserDto updateUser(ReqUpdateUserDto reqUser)
    {
        Optional<User> userOptional = this.userRepository.findById(reqUser.getId());
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            ResUpdateUserDto updatedUser = new ResUpdateUserDto();
            updatedUser.setUsername(user.getUsername());
            updatedUser.setPhone(user.getPhone());

            return updatedUser;
        }

        return null;
    }

    public void deleteUser(long id) {
        this.userRepository.deleteById(id);
    }

    public void updateUserToken(String token, String email){
        User currentUser = this.getUserByEmail(email);
        if (currentUser != null){
            currentUser.setRefreshToken(token);
            this.userRepository.save(currentUser);
        }
    }


    public User getUserByRefreshTokenAndEmail(String refreshToken, String email) {
        return this.userRepository.findByRefreshTokenAndEmail(refreshToken, email);
    }

}
