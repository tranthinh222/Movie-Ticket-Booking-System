package com.cinema.ticketbooking.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Lazy;

import com.cinema.ticketbooking.domain.Booking;
import com.cinema.ticketbooking.domain.Payment;
import com.cinema.ticketbooking.domain.User;
import com.cinema.ticketbooking.domain.response.ResBookingDto;
import com.cinema.ticketbooking.domain.response.ResCreateBookingDto;
import com.cinema.ticketbooking.domain.response.ResRevenueDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.repository.BookingRepository;
import com.cinema.ticketbooking.util.constant.BookingStatusEnum;
import com.cinema.ticketbooking.util.constant.PaymentMethodEnum;
import com.cinema.ticketbooking.util.constant.PaymentStatusEnum;

import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {
    private final BookingRepository bookingRepo;
    private final UserService userService;
    private final BookingItemService bookingItemService;
    private final PaymentService paymentService;
    private final VNPayService vnPayService;

    BookingService(BookingRepository bookingRepo, UserService userService,
            BookingItemService bookingItemService, PaymentService paymentService,
            @Lazy VNPayService vnPayService) {
        this.bookingRepo = bookingRepo;
        this.userService = userService;
        this.bookingItemService = bookingItemService;
        this.paymentService = paymentService;
        this.vnPayService = vnPayService;
    }

    public ResCreateBookingDto createBooking(Long id, PaymentMethodEnum paymentMethod, String ipAddress) {
        Booking booking = new Booking();
        User user = this.userService.getUserById(id);

        booking.setUser(user);
        booking.setStatus(BookingStatusEnum.PENDING);
        Booking savedBooking = this.bookingRepo.save(booking);

        Double total_price = this.bookingItemService.createListItem(id, savedBooking);

        savedBooking.setTotal_price(total_price);
        Booking finalBooking = this.bookingRepo.save(savedBooking);

        // Create payment automatically
        Payment payment = this.paymentService.createPayment(finalBooking, paymentMethod);

        // Create response DTO
        ResCreateBookingDto response = new ResCreateBookingDto();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setPrice(total_price);
        response.setCreatedAt(finalBooking.getCreatedAt());
        response.setPaymentId(payment.getId());

        // If not CASH, automatically create payment URL
        if (paymentMethod != PaymentMethodEnum.CASH) {
            try {
                String paymentUrl = null;
                String orderInfo = "Thanh toan ve xem phim #" + finalBooking.getId();

                if (paymentMethod == PaymentMethodEnum.VNPAY) {
                    paymentUrl = vnPayService.createPaymentUrl(payment.getId(), total_price, orderInfo, ipAddress);
                }

                response.setPaymentUrl(paymentUrl);
            } catch (Exception e) {
                throw new RuntimeException("Failed to create payment URL: " + e.getMessage());
            }
        }

        return response;
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
        dto.setQrCode(booking.getQrCode());
        dto.setCreatedAt(booking.getCreatedAt());
        dto.setUpdatedAt(booking.getUpdatedAt());
        dto.setCreatedBy(booking.getCreatedBy());
        dto.setUpdatedBy(booking.getUpdatedBy());

        // Map user info
        if (booking.getUser() != null) {
            ResBookingDto.UserInfo userInfo = new ResBookingDto.UserInfo();
            userInfo.setId(booking.getUser().getId());
            userInfo.setName(booking.getUser().getUsername());
            dto.setUser(userInfo);
        }

        // Map seats, showtime, film, theater from booking items
        if (booking.getBookingItems() != null && !booking.getBookingItems().isEmpty()) {
            // Map seats
            java.util.List<ResBookingDto.SeatInfo> seats = booking.getBookingItems().stream()
                    .map(item -> new ResBookingDto.SeatInfo(
                            item.getSeat().getId(),
                            item.getSeat().getSeatRow(),
                            item.getSeat().getNumber(),
                            item.getPrice()))
                    .collect(Collectors.toList());
            dto.setSeats(seats);

            // Get showtime, film, theater from first booking item
            com.cinema.ticketbooking.domain.BookingItem firstItem = booking.getBookingItems().get(0);
            if (firstItem.getShowTime() != null) {
                com.cinema.ticketbooking.domain.ShowTime showtime = firstItem.getShowTime();

                // Map showtime info
                ResBookingDto.ShowTimeInfo showtimeInfo = new ResBookingDto.ShowTimeInfo(
                        showtime.getId(),
                        showtime.getDate(),
                        showtime.getStartTime(),
                        showtime.getEndTime(),
                        showtime.getAuditorium() != null ? String.valueOf(showtime.getAuditorium().getNumber())
                                : null);
                dto.setShowtime(showtimeInfo);

                // Map film info
                if (showtime.getFilm() != null) {
                    com.cinema.ticketbooking.domain.Film film = showtime.getFilm();
                    ResBookingDto.FilmInfo filmInfo = new ResBookingDto.FilmInfo(
                            film.getId(),
                            film.getName(),
                            film.getDirector(),
                            film.getActors(),
                            film.getDuration(),
                            film.getDescription(),
                            film.getGenre(),
                            film.getLanguage(),
                            film.getReleaseDate(),
                            film.getStatus(),
                            film.getThumbnail());
                    dto.setFilm(filmInfo);
                }

                // Map theater info
                if (showtime.getAuditorium() != null && showtime.getAuditorium().getTheater() != null) {
                    com.cinema.ticketbooking.domain.Theater theater = showtime.getAuditorium().getTheater();
                    String addressStr = null;
                    if (theater.getAddress() != null) {
                        addressStr = String.format("%s %s, %s",
                                theater.getAddress().getStreet_number() != null
                                        ? theater.getAddress().getStreet_number()
                                        : "",
                                theater.getAddress().getStreet_name() != null
                                        ? theater.getAddress().getStreet_name()
                                        : "",
                                theater.getAddress().getCity() != null ? theater.getAddress().getCity() : "").trim();
                    }
                    ResBookingDto.TheaterInfo theaterInfo = new ResBookingDto.TheaterInfo(
                            theater.getId(),
                            theater.getName(),
                            addressStr);
                    dto.setTheater(theaterInfo);
                }
            }
        }

        // Map payment ID (get first payment if exists)
        if (booking.getPayments() != null && !booking.getPayments().isEmpty()) {
            dto.setPaymentId(booking.getPayments().get(0).getId());
        }

        return dto;
    }

    public Booking getBookingById(Long bookingId) {
        return this.bookingRepo.findByIdWithDetails(bookingId)
                .orElse(null);
    }

    public Booking updateBooking(Booking booking) {
        return this.bookingRepo.save(booking);
    }

    public Booking updateBookingStatus(Long bookingId, BookingStatusEnum status) {
        Booking booking = this.bookingRepo.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found with id: " + bookingId));
        booking.setStatus(status);

        // Update payment status to PAID when booking is CONFIRMED
        if (status == BookingStatusEnum.CONFIRMED) {
            List<Payment> payments = this.paymentService.getPaymentsByBookingId(bookingId);
            for (Payment payment : payments) {
                payment.setStatus(PaymentStatusEnum.PAID);
                this.paymentService.savePayment(payment);
            }
        }

        return this.bookingRepo.save(booking);
    }

    public void deleteBooking(Long bookingId) {
        this.bookingRepo.deleteById(bookingId);
    }

    public ResultPaginationDto getBookingsByUserId(Long userId, Pageable pageable) {
        // Step 1: Get paginated IDs only
        Page<Long> pageIds = this.bookingRepo.findIdsByUserId(userId, pageable);

        // Step 2: Fetch booking details with JOIN FETCH
        List<Long> bookingIds = pageIds.getContent();
        List<Booking> bookingsWithDetails = bookingIds.isEmpty()
                ? new java.util.ArrayList<>()
                : this.bookingRepo.findByIdsWithDetails(bookingIds);

        // Convert to DTOs
        List<ResBookingDto> bookingDtos = bookingsWithDetails.stream()
                .map(this::convertToResBookingDto)
                .collect(Collectors.toList());

        // Build pagination result
        ResultPaginationDto resultPaginationDto = new ResultPaginationDto();
        ResultPaginationDto.Meta mt = new ResultPaginationDto.Meta();

        mt.setCurrentPage(pageable.getPageNumber() + 1);
        mt.setTotalPages(pageIds.getTotalPages());
        mt.setPageSize(pageable.getPageSize());
        mt.setTotalItems(pageIds.getTotalElements());

        resultPaginationDto.setMeta(mt);
        resultPaginationDto.setData(bookingDtos);

        return resultPaginationDto;
    }

    /**
     * Lấy doanh thu trong tháng hiện tại
     * 
     * @return ResRevenueDto chứa thông tin doanh thu
     */
    public ResRevenueDto getCurrentMonthRevenue() {
        // Lấy thời gian bắt đầu và kết thúc của tháng hiện tại
        YearMonth currentMonth = YearMonth.now();
        Instant startOfMonth = currentMonth.atDay(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();
        Instant startOfNextMonth = currentMonth.plusMonths(1).atDay(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();

        return getRevenueByDateRange(currentMonth.getMonthValue(), currentMonth.getYear(),
                startOfMonth, startOfNextMonth);
    }

    /**
     * Lấy doanh thu theo tháng và năm cụ thể
     * 
     * @param month tháng (1-12)
     * @param year  năm
     * @return ResRevenueDto chứa thông tin doanh thu
     */
    public ResRevenueDto getRevenueByMonth(int month, int year) {
        YearMonth yearMonth = YearMonth.of(year, month);
        Instant startOfMonth = yearMonth.atDay(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();
        Instant startOfNextMonth = yearMonth.plusMonths(1).atDay(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();

        return getRevenueByDateRange(month, year, startOfMonth, startOfNextMonth);
    }

    private ResRevenueDto getRevenueByDateRange(int month, int year, Instant startDate, Instant endDate) {
        // Lấy tổng doanh thu
        Double totalRevenue = this.bookingRepo.getTotalRevenueByDateRange(startDate, endDate);

        // Lấy số lượng booking
        Long totalBookings = this.bookingRepo.countBookingsByDateRange(startDate, endDate);

        return new ResRevenueDto(month, year, totalRevenue, totalBookings);
    }
}
