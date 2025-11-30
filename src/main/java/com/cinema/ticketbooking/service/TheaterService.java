package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.Address;
import com.cinema.ticketbooking.domain.Theater;
import com.cinema.ticketbooking.domain.request.ReqCreateTheaterDto;
import com.cinema.ticketbooking.domain.request.ReqUpdateTheaterDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.repository.AddressRepository;
import com.cinema.ticketbooking.repository.TheaterRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TheaterService {
    private final TheaterRepository theaterRepository;
    private final AddressRepository addressRepository;
    public TheaterService(TheaterRepository theaterRepository,  AddressRepository addressRepository) {
        this.theaterRepository = theaterRepository;
        this.addressRepository = addressRepository;
    }

    @Transactional
    public ResultPaginationDto getAllTheaters(Specification<Theater> spec, Pageable pageable) {
        Page<Theater> theaterPage = theaterRepository.findAll(spec, pageable);
        ResultPaginationDto resultPaginationDto = new ResultPaginationDto();
        ResultPaginationDto.Meta mt = new ResultPaginationDto.Meta();

        mt.setCurrentPage(pageable.getPageNumber() + 1);
        mt.setTotalPages(pageable.getPageSize());
        mt.setPageSize(theaterPage.getTotalPages());
        mt.setCurrentPage(theaterPage.getTotalElements());

        resultPaginationDto.setMeta(mt);
        resultPaginationDto.setData(theaterPage.getContent());

        return resultPaginationDto;
    }

    public Theater createTheater(ReqCreateTheaterDto reqTheater) {
        Theater theater = new Theater();
        theater.setName(reqTheater.getName());
        if (reqTheater.getAddressId() != null){
            Optional<Address> optionalAddress = addressRepository.findById(reqTheater.getAddressId());
            theater.setAddress(optionalAddress.isPresent() ? optionalAddress.get():null);
        }

        return this.theaterRepository.save(theater);
    }

    public Theater findTheaterById(Long id){
        return this.theaterRepository.findById(id).orElse(null);
    }

    public void deleteTheater(Long id) {
        this.theaterRepository.deleteById(id);
    }

    public Theater updateTheater(ReqUpdateTheaterDto reqUpdateTheaterDto){
        Theater theater = findTheaterById(reqUpdateTheaterDto.getId());
        if (theater == null){
            return null;
        }

        theater.setName(reqUpdateTheaterDto.getName());
        return this.theaterRepository.save(theater);
    }
}
