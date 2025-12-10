package com.cinema.ticketbooking.domain.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.Instant;

@Data
public class ResAuditoriumDto {
    private Long id;
    private Long number;
    private Long totalSeats;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a", timezone = "GMT+7")
    private Instant createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a", timezone = "GMT+7")
    private Instant updatedAt;

    private String createdBy;
    private String updatedBy;
}

