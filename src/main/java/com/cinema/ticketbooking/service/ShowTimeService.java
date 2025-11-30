package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.repository.ShowTimeRepository;
import org.springframework.stereotype.Service;

@Service
public class ShowTimeService {
    private final ShowTimeRepository showTimeRepository;
    public ShowTimeService (ShowTimeRepository showTimeRepository){
        this.showTimeRepository = showTimeRepository;
    }




}
