package com.cinema.ticketbooking.domain;

import com.cinema.ticketbooking.util.SecurityUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "showtimes")
@Data
public class ShowTime {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        @JoinColumn(name = "film_id")
        private Film film;

        @ManyToOne
        @JoinColumn(name = "auditorium")
        private Auditorium auditorium;

        // @OneToMany(mappedBy = "showTime", fetch = FetchType.LAZY, cascade =
        // CascadeType.ALL, orphanRemoval = true)
        // @JsonIgnore
        // List<Booking> bookings;

        @OneToMany(mappedBy = "showTime", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
        @JsonIgnore
        List<SeatHold> seatHolds;

        private LocalDate date;
        private LocalTime startTime;
        private LocalTime endTime;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a", timezone = "GMT+7")
        private Instant createdAt;
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a", timezone = "GMT+7")
        private Instant updatedAt;
        private String createdBy;
        private String updatedBy;

        @PrePersist
        public void handleBeforeCreated() {
                this.createdBy = SecurityUtil.getCurrentUserLogin().isPresent()
                                ? SecurityUtil.getCurrentUserLogin().get()
                                : "";
                this.createdAt = Instant.now();
        }

        @PreUpdate
        public void handleBeforeUpdated() {
                this.updatedBy = SecurityUtil.getCurrentUserLogin().isPresent()
                                ? SecurityUtil.getCurrentUserLogin().get()
                                : "";
                this.updatedAt = Instant.now();
        }
}
