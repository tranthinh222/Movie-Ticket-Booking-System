package com.cinema.ticketbooking.domain;

import com.cinema.ticketbooking.util.SecurityUtil;
import com.cinema.ticketbooking.util.constant.SeatTypeEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "seat_variants")
@Data
public class SeatVariant {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @OneToMany(mappedBy = "seatVariant", fetch = FetchType.LAZY)
        @JsonIgnore
        private List<Seat> seats;

        @Enumerated(EnumType.STRING)
        private SeatTypeEnum seatType;

        @Column(nullable = false)
        private double basePrice;

        @Column(nullable = true)
        private double bonus;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a", timezone = "GMT+7")
        private Instant createdAt;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a", timezone = "GMT+7")
        private Instant updatedAt;

        private String createdBy;
        private String updatedBy;

        @PrePersist
        public void handleBeforeCreated() {
                this.createdBy = SecurityUtil.getCurrentUserLogin().orElse("");
                this.createdAt = Instant.now();
        }

        @PreUpdate
        public void handleBeforeUpdated() {
                this.updatedBy = SecurityUtil.getCurrentUserLogin().orElse("");
                this.updatedAt = Instant.now();
        }
}
