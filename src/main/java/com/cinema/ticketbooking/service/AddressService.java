package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.Address;
import com.cinema.ticketbooking.domain.Theater;
import com.cinema.ticketbooking.domain.request.ReqCreateAddressDto;
import com.cinema.ticketbooking.domain.request.ReqUpdateAddressDto;
import com.cinema.ticketbooking.domain.response.ResAuditoriumDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.repository.AddressRepository;
import com.cinema.ticketbooking.repository.projection.AddressWithTheatersProjection;
import com.cinema.ticketbooking.repository.projection.TheaterIdNameProjection;
import com.cinema.ticketbooking.util.error.BadRequestException;
import com.cinema.ticketbooking.util.error.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AddressService {
    private final AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public ResultPaginationDto getAllAddresses(Specification<Address> spec, Pageable pageable) {
        Page<Address> pageAddress = this.addressRepository.findAll(spec, pageable);
        ResultPaginationDto resultPaginationDto = new ResultPaginationDto();
        ResultPaginationDto.Meta mt = new ResultPaginationDto.Meta();

        mt.setCurrentPage(pageable.getPageNumber() + 1);
        mt.setTotalPages(pageAddress.getTotalPages());
        mt.setPageSize(pageable.getPageSize());
        mt.setTotalItems(pageAddress.getTotalElements());

        resultPaginationDto.setMeta(mt);
        resultPaginationDto.setData(pageAddress.getContent());

        return resultPaginationDto;
    }

    public Address createAddress(ReqCreateAddressDto reqAddress) {
        Address address = new Address();
        address.setCity(reqAddress.getCity());
        address.setStreet_name(reqAddress.getStreet_name());
        address.setStreet_number(reqAddress.getStreet_number());

        this.addressRepository.save(address);
        return address;
    }

    public Address findAddressById(Long id) {
        return this.addressRepository.findById(id).orElse(null);
    }

    public void deleteAddress(Long id) {
        this.addressRepository.deleteById(id);
    }

    private boolean hasUpdatableField(ReqUpdateAddressDto req) {
        return (req.getCity() != null && !req.getCity().trim().isEmpty())
                || (req.getStreet_name() != null && !req.getStreet_name().trim().isEmpty())
                || (req.getStreet_number() != null && !req.getStreet_number().trim().isEmpty());
    }

    public Address updateAddress(ReqUpdateAddressDto reqAddress) {
        if (!hasUpdatableField(reqAddress)) {
            throw new BadRequestException("No data provided for update");
        }

        Address address = findAddressById(reqAddress.getId());
        if (address == null)
            return null;

        Optional.ofNullable(reqAddress.getCity())
                .filter(city -> !city.trim().isEmpty())
                .ifPresent(city -> address.setCity(city));

        Optional.ofNullable(reqAddress.getStreet_name())
                .filter(name -> !name.trim().isEmpty())
                .ifPresent(name -> address.setStreet_name(name));

        Optional.ofNullable(reqAddress.getStreet_number())
                .filter(number -> !number.trim().isEmpty())
                .ifPresent(number -> address.setStreet_number(number));

        this.addressRepository.save(address);
        return address;
    }

    public List<TheaterIdNameProjection> getTheatersByAddressId(Long addressId) {
        AddressWithTheatersProjection address = addressRepository
                .findProjectedById(addressId)
                .orElseThrow(() -> new NotFoundException("Address with id " + addressId + " not found"));

        return address.getTheaters();
    }
}
