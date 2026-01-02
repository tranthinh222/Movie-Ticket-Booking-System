package com.cinema.ticketbooking.domain.response;

import com.cinema.ticketbooking.util.constant.BookingStatusEnum;
import com.cinema.ticketbooking.util.constant.FilmStatusEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResBookingDto {
    private Long id;
    private UserInfo user;
    private BookingStatusEnum status;
    private Double total_price;
    private String qrCode;
    private List<SeatInfo> seats;
    private ShowTimeInfo showtime;
    private FilmInfo film;
    private TheaterInfo theater;
    private Long paymentId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a", timezone = "GMT+7")
    private Instant createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a", timezone = "GMT+7")
    private Instant updatedAt;

    private String createdBy;
    private String updatedBy;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UserInfo {
        private Long id;
        private String name;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SeatInfo {
        private Long id;
        private String seatRow;
        private Integer number;
        private Double price;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ShowTimeInfo {
        private Long id;
        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;
        private String auditoriumNumber;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FilmInfo {
        private Long id;
        private String name;
        private String director;
        private String actors;
        private Long duration;
        private String description;
        private String genre;
        private String language;
        private LocalDate releaseDate;
        private FilmStatusEnum status;
        private String thumbnail;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class TheaterInfo {
        private Long id;
        private String name;
        private String address;
    }
}
