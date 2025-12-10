package com.cinema.ticketbooking.domain;

import com.cinema.ticketbooking.util.SecurityUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "films")
@Data
public class Film {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "film", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    List<ShowTime> showTime;

    @Column(unique = true)
    private String name;
    private Long duration;
    private Long price;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String description;
    private String genre;
    private String language;
    @Column(name = "release_date")
    private LocalDate releaseDate;
    private Long rating;

    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String updatedBy;

    @PrePersist
    public void handleBeforeCreated(){
        this.createdBy = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get() : "";
        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void handleBeforeUpdated(){
        this.updatedBy = SecurityUtil.getCurrentUserLogin().isPresent()
                ? SecurityUtil.getCurrentUserLogin().get() : "";
        this.updatedAt = Instant.now();
    }
}
