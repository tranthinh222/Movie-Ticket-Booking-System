package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.Address;
import com.cinema.ticketbooking.domain.request.ReqCreateAddressDto;
import com.cinema.ticketbooking.domain.request.ReqUpdateAddressDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.repository.AddressRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class AddressService {
    private final AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public ResultPaginationDto getAllAddresses(Specification<Address> spec, Pageable pageable) {
        Page<Address> pageAddress =  this.addressRepository.findAll(spec, pageable);
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

    public Address updateAddress(ReqUpdateAddressDto reqAddress) {
        Address address = findAddressById(reqAddress.getId());
        if (address == null)
            return null;

        if (reqAddress.getCity() != null)
            address.setCity(reqAddress.getCity());
        if (reqAddress.getStreet_name() != null)
            address.setStreet_name(reqAddress.getStreet_name());
        if (reqAddress.getStreet_number() != null)
            address.setStreet_number(reqAddress.getStreet_number());
        this.addressRepository.save(address);

        return address;
    }
}
