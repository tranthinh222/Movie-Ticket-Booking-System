package com.cinema.ticketbooking.domain;

import com.cinema.ticketbooking.util.SecurityUtil;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "auditoriums")
@Data
public class Auditorium {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @OneToMany(mappedBy = "auditorium", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
        @JsonIgnore
        List<ShowTime> showTimes;

        @ManyToOne
        @JoinColumn(name = "theater_id")
        private Theater theater;

        @OneToMany(mappedBy = "auditorium", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
        @JsonIgnore
        List<Seat> seats;

        private Long number;
        private Long totalSeats;

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
