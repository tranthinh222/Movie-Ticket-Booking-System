package com.cinema.ticketbooking.controller;

import com.cinema.ticketbooking.domain.Address;
import com.cinema.ticketbooking.domain.request.ReqCreateAddressDto;
import com.cinema.ticketbooking.domain.request.ReqUpdateAddressDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.repository.projection.TheaterIdNameProjection;
import com.cinema.ticketbooking.service.AddressService;
import com.cinema.ticketbooking.service.TheaterService;
import com.cinema.ticketbooking.util.annotation.ApiMessage;
import com.cinema.ticketbooking.util.error.IdInvalidException;
import com.turkraft.springfilter.boot.Filter;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1")
public class AddressController {
    private final AddressService addressService;
    private final TheaterService theaterService;

    public AddressController(AddressService addressService, TheaterService theaterService) {
        this.addressService = addressService;
        this.theaterService = theaterService;
    }

    @GetMapping("/addresses")
    @ApiMessage("fetch all addresses")
    public ResponseEntity<ResultPaginationDto> getAllAddresses(
            @Filter Specification<Address> spec, Pageable pageable) {

        return ResponseEntity.status(HttpStatus.OK).body(this.addressService.getAllAddresses(spec, pageable));
    }

    @GetMapping("/addresses/{id}")
    @ApiMessage("fetch an address")
    public ResponseEntity<Address> getAddressById(@PathVariable("id") Long id) {
        Address address = this.addressService.findAddressById(id);
        if (address == null) {
            throw new IdInvalidException("Address with id " + id + " not found");
        }
        return ResponseEntity.status(HttpStatus.OK).body(address);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/addresses")
    @ApiMessage("create an address")
    public ResponseEntity<Address> createAddress(@Valid @RequestBody ReqCreateAddressDto reqAddress) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.addressService.createAddress(reqAddress));

    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/addresses/{id}")
    @ApiMessage("delete an address")
    public ResponseEntity<Void> deleteAddress(@PathVariable("id") Long id) {
        Address address = this.addressService.findAddressById(id);
        if (address == null) {
            throw new IdInvalidException("address with id " + id + " not found");
        }
        this.addressService.deleteAddress(id);
        return ResponseEntity.status(HttpStatus.OK).body(null);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/addresses")
    public ResponseEntity<Address> updateAddress(@Valid @RequestBody ReqUpdateAddressDto reqAddress) {
        Address newAddress = this.addressService.updateAddress(reqAddress);
        if (newAddress == null) {
            throw new IdInvalidException("Address with id " + reqAddress.getId() + " does not exist");
        }

        return ResponseEntity.status(HttpStatus.OK).body(newAddress);
    }

    @GetMapping("/theaters/address/{id}")
    @ApiMessage("fetch theaters by address")
    public ResponseEntity<List<TheaterIdNameProjection>> getTheatersByAddressId(@PathVariable("id") Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(this.addressService.getTheatersByAddressId(id));
    }

}
