package com.cinema.ticketbooking.controller;

import com.cinema.ticketbooking.domain.News;
import com.cinema.ticketbooking.domain.request.ReqNewsDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.service.NewsService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class NewsController {
    private final NewsService service;
    public NewsController(NewsService service) { this.service = service; }
    @GetMapping("/news")
    public ResultPaginationDto list(@RequestParam(required = false) String category,
            @RequestParam(required = false) String search, Pageable pageable) {
        return service.list(category, search, pageable, false);
    }
    @GetMapping("/news/{id}")
    public News get(@PathVariable Long id) { return service.get(id, false); }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/news")
    public ResultPaginationDto adminList(@RequestParam(required = false) String category,
            @RequestParam(required = false) String search, Pageable pageable) {
        return service.list(category, search, pageable, true);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/news")
    public ResponseEntity<News> create(@Valid @RequestBody ReqNewsDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.save(null, request));
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/news/{id}")
    public News update(@PathVariable Long id, @Valid @RequestBody ReqNewsDto request) { return service.save(id, request); }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/news/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) { service.delete(id); return ResponseEntity.noContent().build(); }
}
