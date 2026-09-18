package com.cinema.ticketbooking.domain;
import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.*;
@Entity @Table(name="discount") @Data
public class Discount {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true,length=40) private String code;
 @Column(nullable=false) private String title;
 @Column(length=4000) private String description;
 @Column(length=2000) private String image;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private Type type;
 @Column(name="discount_value",nullable=false,precision=15,scale=2) private BigDecimal value;
 @Column(nullable=false,precision=15,scale=2) private BigDecimal minOrder;
 @Column(precision=15,scale=2) private BigDecimal maxDiscount;
 @Column(nullable=false) private LocalDate startsOn;
 @Column(nullable=false) private LocalDate endsOn;
 private boolean active;
 private String seedKey;
 private Instant createdAt;
 private Instant updatedAt;
 public enum Type { FIXED, PERCENT }
 @PrePersist void create(){createdAt=Instant.now();updatedAt=createdAt;}
 @PreUpdate void update(){updatedAt=Instant.now();}
}
