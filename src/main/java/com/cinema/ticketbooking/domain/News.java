package com.cinema.ticketbooking.domain;

import jakarta.persistence.*;
import lombok.Data;
import java.time.Instant;

@Entity
@Table(name = "news")
@Data
public class News {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, length = 80)
    private String seedKey;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false, length = 2000)
    private String summary;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;
    @Column(length = 2000)
    private String image;
    @Column(nullable = false)
    private String category;
    private boolean featured;
    private boolean published;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    private Instant updatedAt;
    @PrePersist
    void create() { createdAt = Instant.now(); updatedAt = createdAt; }
    @PreUpdate
    void update() { updatedAt = Instant.now(); }
}
