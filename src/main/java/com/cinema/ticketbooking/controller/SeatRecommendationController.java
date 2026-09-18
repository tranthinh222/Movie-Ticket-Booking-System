package com.cinema.ticketbooking.controller;
import com.cinema.ticketbooking.domain.request.ReqSeatRecommendationDto;
import com.cinema.ticketbooking.domain.response.ResSeatRecommendationDto;
import com.cinema.ticketbooking.service.SeatRecommendationService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/v1/assistant")
public class SeatRecommendationController {
 private final SeatRecommendationService service;
 public SeatRecommendationController(SeatRecommendationService service){this.service=service;}
 @PostMapping("/seat-recommendations") public List<ResSeatRecommendationDto> recommend(@Valid @RequestBody ReqSeatRecommendationDto request){return service.recommend(request);}
}
