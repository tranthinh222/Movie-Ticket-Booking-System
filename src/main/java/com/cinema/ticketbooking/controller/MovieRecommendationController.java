package com.cinema.ticketbooking.controller;

import com.cinema.ticketbooking.domain.request.ReqMovieRecommendationDto;
import com.cinema.ticketbooking.domain.response.ResMovieRecommendationDto;
import com.cinema.ticketbooking.service.MovieRecommendationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/assistant")
public class MovieRecommendationController {
    private final MovieRecommendationService service;

    public MovieRecommendationController(MovieRecommendationService service) {
        this.service = service;
    }

    @PostMapping("/recommendations")
    public List<ResMovieRecommendationDto> recommend(@Valid @RequestBody ReqMovieRecommendationDto request) {
        return service.recommend(request);
    }
}
