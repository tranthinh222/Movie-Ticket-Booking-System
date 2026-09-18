package com.cinema.ticketbooking.domain.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReqNewsDto {
    @NotBlank @Size(max = 255)
    private String title;
    @NotBlank @Size(max = 2000)
    private String summary;
    @NotBlank @Size(max = 100000)
    private String content;
    @Size(max = 2000)
    @Pattern(regexp = "^(https?://[^\\s]+)?$")
    private String image;
    @NotBlank
    @Pattern(regexp = "Tin mới|Review phim|Sắp chiếu|Diễn viên")
    private String category;
    private boolean featured;
    private boolean published;
}
