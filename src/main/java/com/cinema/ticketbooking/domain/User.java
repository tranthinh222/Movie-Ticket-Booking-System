package com.cinema.ticketbooking.domain;

import com.cinema.ticketbooking.util.SecurityUtil;
import com.cinema.ticketbooking.util.constant.RoleEnum;
import com.cinema.ticketbooking.util.constant.UserGenderEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class User {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private long id;

        @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
        @JsonIgnore
        List<Booking> bookings;

        @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
        @JsonIgnore
        List<SeatHold> seatHolds;

        private String username;
        private String email;
        private String password;
        private UserGenderEnum gender;
        private String phone;
        @Column(columnDefinition = "TEXT")
        private String avatar;
        @Column(columnDefinition = "MEDIUMTEXT")
        private String refreshToken;

        @Enumerated(EnumType.STRING)
        private RoleEnum role;
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
