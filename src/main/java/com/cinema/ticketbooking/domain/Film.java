package com.cinema.ticketbooking.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table (name = "films")
@Data
public class Film {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank (message = "film name is not empty")
    @Column(unique = true)
    String name;
    Long duration;
    Long price;

    @Column(columnDefinition = "MEDIUMTEXT")
    String description;
    String genre;
    String language;
    LocalDate release_date;
    Long rating;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a", timezone = "GMT+7")
    Instant created_at;
    Instant updated_at;

    @PrePersist
    public void handleBeforeCreated() {
        created_at = Instant.now();
        updated_at = Instant.now();
    }

    @PreUpdate
    public void handleBeforeUpdated() {
        updated_at = Instant.now();
    }
}
