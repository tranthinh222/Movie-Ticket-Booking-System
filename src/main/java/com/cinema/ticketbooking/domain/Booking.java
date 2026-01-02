package com.cinema.ticketbooking.domain;

import com.cinema.ticketbooking.util.SecurityUtil;
import com.cinema.ticketbooking.util.constant.BookingStatusEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "bookings")
@Data
public class Booking {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        // @ManyToOne
        // @JoinColumn(name = "showtime_id")
        // private ShowTime showTime;

        @ManyToOne
        @JoinColumn(name = "user_id")
        private User user;

        @OneToMany(mappedBy = "booking", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
        @JsonIgnore
        List<Payment> payments;

        @OneToMany(mappedBy = "booking", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
        @JsonIgnore
        List<BookingItem> bookingItems;

        @Enumerated(EnumType.STRING)
        private BookingStatusEnum status;
        private Double total_price;

        @Column(columnDefinition = "TEXT")
        private String qrCode; // Base64 encoded QR code or QR code URL

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
