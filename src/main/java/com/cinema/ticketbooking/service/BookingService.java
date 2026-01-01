package com.cinema.ticketbooking.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.cinema.ticketbooking.domain.Booking;
import com.cinema.ticketbooking.domain.User;
import com.cinema.ticketbooking.domain.response.ResBookingDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.repository.BookingRepository;
import com.cinema.ticketbooking.util.constant.BookingStatusEnum;
import com.cinema.ticketbooking.util.constant.MethodEnum;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {
    private final BookingRepository bookingRepo;
    private final UserService userService;
    private final BookingItemService bookingItemService;
    private final PaymentService paymentService;

    BookingService(BookingRepository bookingRepo, UserService userService,
            BookingItemService bookingItemService, PaymentService paymentService) {
        this.bookingRepo = bookingRepo;
        this.userService = userService;
        this.bookingItemService = bookingItemService;
        this.paymentService = paymentService;
    }

    public Booking createBooking(Long id, MethodEnum paymentMethod) {
        Booking booking = new Booking();
        User user = this.userService.getUserById(id);

        booking.setUser(user);
        booking.setStatus(BookingStatusEnum.PENDING);
        Booking savedBooking = this.bookingRepo.save(booking);

        Double total_price = this.bookingItemService.createListItem(id, savedBooking);

        savedBooking.setTotal_price(total_price);
        Booking finalBooking = this.bookingRepo.save(savedBooking);

        // Create payment automatically
        this.paymentService.createPayment(finalBooking, paymentMethod);

        return finalBooking;
    }

    public ResultPaginationDto getAllBookings(Specification<Booking> spec, Pageable pageable) {
        Page<Booking> pageBooking = this.bookingRepo.findAll(spec, pageable);

        // Convert Booking to ResBookingDto
        List<ResBookingDto> bookingDtos = pageBooking.getContent().stream()
                .map(this::convertToResBookingDto)
                .collect(Collectors.toList());

        ResultPaginationDto resultPaginationDto = new ResultPaginationDto();
        ResultPaginationDto.Meta mt = new ResultPaginationDto.Meta();

        mt.setCurrentPage(pageable.getPageNumber() + 1);
        mt.setTotalPages(pageBooking.getTotalPages());
        mt.setPageSize(pageable.getPageSize());
        mt.setTotalItems(pageBooking.getTotalElements());

        resultPaginationDto.setMeta(mt);
        resultPaginationDto.setData(bookingDtos);

        return resultPaginationDto;
    }

    private ResBookingDto convertToResBookingDto(Booking booking) {
        ResBookingDto dto = new ResBookingDto();
        dto.setId(booking.getId());
        dto.setStatus(booking.getStatus());
        dto.setTotal_price(booking.getTotal_price());
        dto.setCreatedAt(booking.getCreatedAt());
        dto.setUpdatedAt(booking.getUpdatedAt());
        dto.setCreatedBy(booking.getCreatedBy());
        dto.setUpdatedBy(booking.getUpdatedBy());

        // Chỉ lấy user.id và user.username
        if (booking.getUser() != null) {
            ResBookingDto.UserInfo userInfo = new ResBookingDto.UserInfo();
            userInfo.setId(booking.getUser().getId());
            userInfo.setName(booking.getUser().getUsername());
            dto.setUser(userInfo);
        }

        return dto;
    }

    public Booking getBookingById(Long bookingId) {
        return this.bookingRepo.findById(bookingId)
                .orElse(null);
    }
}
