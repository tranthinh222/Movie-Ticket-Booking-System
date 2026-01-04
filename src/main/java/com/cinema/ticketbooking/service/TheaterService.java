package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.Address;
import com.cinema.ticketbooking.domain.Auditorium;
import com.cinema.ticketbooking.domain.Theater;
import com.cinema.ticketbooking.domain.request.ReqCreateTheaterDto;
import com.cinema.ticketbooking.domain.request.ReqUpdateTheaterDto;
import com.cinema.ticketbooking.domain.response.ResAuditoriumDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.repository.AddressRepository;
import com.cinema.ticketbooking.repository.TheaterRepository;
import com.cinema.ticketbooking.util.error.BadRequestException;
import com.cinema.ticketbooking.util.error.NotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TheaterService {
    private final TheaterRepository theaterRepository;
    private final AddressRepository addressRepository;

    public TheaterService(TheaterRepository theaterRepository, AddressRepository addressRepository) {
        this.theaterRepository = theaterRepository;
        this.addressRepository = addressRepository;
    }

    @Transactional
    public ResultPaginationDto getAllTheaters(Specification<Theater> spec, Pageable pageable) {
        Page<Theater> theaterPage = theaterRepository.findAll(spec, pageable);
        ResultPaginationDto resultPaginationDto = new ResultPaginationDto();
        ResultPaginationDto.Meta mt = new ResultPaginationDto.Meta();

        mt.setCurrentPage(pageable.getPageNumber() + 1);
        mt.setTotalPages(theaterPage.getTotalPages());
        mt.setPageSize(pageable.getPageSize());
        mt.setTotalItems(theaterPage.getTotalElements());

        resultPaginationDto.setMeta(mt);
        resultPaginationDto.setData(theaterPage.getContent());

        return resultPaginationDto;
    }

    public Theater createTheater(ReqCreateTheaterDto reqTheater) {
        Theater theater = new Theater();
        theater.setName(reqTheater.getName());
        if (reqTheater.getAddressId() != null) {
            Optional<Address> optionalAddress = addressRepository.findById(reqTheater.getAddressId());
            theater.setAddress(optionalAddress.isPresent() ? optionalAddress.get() : null);
        }

        return this.theaterRepository.save(theater);
    }

    public Theater findTheaterById(Long id) {
        return this.theaterRepository.findById(id).orElse(null);
    }

    public void deleteTheater(Long id) {
        this.theaterRepository.deleteById(id);
    }

    private boolean hasUpdatableField(ReqUpdateTheaterDto req) {
        return req.getName() != null && !req.getName().trim().isEmpty();
    }

    public List<ResAuditoriumDto> getAuditoriumsByTheaterId(Long theaterId) {
        Theater t = theaterRepository.findById(theaterId)
                .orElseThrow(() -> new NotFoundException("Theater not found"));

        return t.getAuditoriums()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public Theater updateTheater(ReqUpdateTheaterDto reqUpdateTheaterDto) {
        if (!hasUpdatableField(reqUpdateTheaterDto)) {
            throw new BadRequestException("No data provided for update");
        }

        Theater theater = findTheaterById(reqUpdateTheaterDto.getId());
        if (theater == null) {
            return null;
        }

        Optional.ofNullable(reqUpdateTheaterDto.getName())
                .filter(name -> !name.trim().isEmpty())
                .ifPresent(name -> theater.setName(name));

        return this.theaterRepository.save(theater);
    }

    private ResAuditoriumDto convertToDto(Auditorium auditorium) {
        ResAuditoriumDto dto = new ResAuditoriumDto();
        dto.setId(auditorium.getId());
        dto.setNumber(auditorium.getNumber());
        dto.setTotalSeats(auditorium.getTotalSeats());
        dto.setCreatedAt(auditorium.getCreatedAt());
        dto.setUpdatedAt(auditorium.getUpdatedAt());
        dto.setCreatedBy(auditorium.getCreatedBy());
        dto.setUpdatedBy(auditorium.getUpdatedBy());
        return dto;
    }
}
