package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.Seat;
import com.cinema.ticketbooking.domain.SeatVariant;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.repository.SeatVariantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class SeatVariantService {
    private final SeatVariantRepository seatVariantRepository;
    public SeatVariantService(SeatVariantRepository seatVariantRepository) {
        this.seatVariantRepository = seatVariantRepository;
    }

    public ResultPaginationDto getAllSeatVariants(Specification<SeatVariant> spec, Pageable pageable) {
        Page<SeatVariant> pageSeatVariant =  this.seatVariantRepository.findAll(spec, pageable);
        ResultPaginationDto resultPaginationDto = new ResultPaginationDto();
        ResultPaginationDto.Meta mt = new ResultPaginationDto.Meta();

        mt.setCurrentPage(pageable.getPageNumber() + 1);
        mt.setTotalPages(pageSeatVariant.getTotalPages());
        mt.setPageSize(pageable.getPageSize());
        mt.setTotalItems(pageSeatVariant.getTotalElements());

        resultPaginationDto.setMeta(mt);
        resultPaginationDto.setData(pageSeatVariant.getContent());

        return resultPaginationDto;
    }

    public SeatVariant findSeatVariantById(Long id) {
        return this.seatVariantRepository.findById(id).orElse(null);
    }

    public void deleteSeatVariant(Long id) {
        this.seatVariantRepository.deleteById(id);
    }

}
