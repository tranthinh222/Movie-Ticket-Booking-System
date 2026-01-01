package com.cinema.ticketbooking.controller;

import com.cinema.ticketbooking.domain.Address;
import com.cinema.ticketbooking.domain.request.ReqCreateAddressDto;
import com.cinema.ticketbooking.domain.request.ReqUpdateAddressDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.service.AddressService;
import com.cinema.ticketbooking.util.error.IdInvalidException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentMatchers;

@ExtendWith(MockitoExtension.class)
class AddressControllerTest {

    @Mock
    private AddressService addressService;

    @InjectMocks
    private AddressController addressController;

    @Test
    void getAllAddresses_shouldReturnOk_whenCalledSuccessfully() {
        // Arrange
        ResultPaginationDto mockResult = new ResultPaginationDto();
        when(addressService.getAllAddresses(ArgumentMatchers.<Specification<Address>>any(), any(Pageable.class)))
                .thenReturn(mockResult);

        // Act
        ResponseEntity<ResultPaginationDto> response =
                addressController.getAllAddresses(null, PageRequest.of(0, 10));

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockResult, response.getBody());
        verify(addressService).getAllAddresses(ArgumentMatchers.<Specification<Address>>any(), any(Pageable.class));
    }

    @Test
    void getAddressById_shouldReturnAddress_whenFound() {
        // Arrange
        Address address = new Address();
        address.setId(1L);
        when(addressService.findAddressById(1L)).thenReturn(address);

        // Act
        ResponseEntity<Address> response = addressController.getAddressById(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(address, response.getBody());
    }

    @Test
    void getAddressById_shouldThrowException_whenNotFound() {
        // Arrange
        when(addressService.findAddressById(1L)).thenReturn(null);

        // Act & Assert
        IdInvalidException exception = assertThrows(
                IdInvalidException.class,
                () -> addressController.getAddressById(1L)
        );

        assertEquals("Address with id 1 not found", exception.getMessage());
    }

    @Test
    void createAddress_shouldReturnCreatedAddress_whenRequestIsValid() {
        // Arrange
        ReqCreateAddressDto request = new ReqCreateAddressDto();
        Address createdAddress = new Address();
        createdAddress.setId(1L);

        when(addressService.createAddress(request)).thenReturn(createdAddress);

        // Act
        ResponseEntity<Address> response = addressController.createAddress(request);

        // Assert
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(createdAddress, response.getBody());
    }

    @Test
    void deleteAddress_shouldReturnOk_whenAddressExists() {
        // Arrange
        Address address = new Address();
        address.setId(1L);
        when(addressService.findAddressById(1L)).thenReturn(address);
        doNothing().when(addressService).deleteAddress(1L);

        // Act
        ResponseEntity<Void> response = addressController.deleteAddress(1L);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        verify(addressService).deleteAddress(1L);
    }

    @Test
    void deleteAddress_shouldThrowException_whenAddressNotFound() {
        // Arrange
        when(addressService.findAddressById(1L)).thenReturn(null);

        // Act & Assert
        IdInvalidException exception = assertThrows(
                IdInvalidException.class,
                () -> addressController.deleteAddress(1L)
        );

        assertEquals("address with id 1 not found", exception.getMessage());
    }

    @Test
    void updateAddress_shouldReturnUpdatedAddress_whenAddressExists() {
        // Arrange
        ReqUpdateAddressDto request = new ReqUpdateAddressDto();
        request.setId(1L);
        Address updatedAddress = new Address();
        updatedAddress.setId(1L);

        when(addressService.updateAddress(request)).thenReturn(updatedAddress);

        // Act
        ResponseEntity<Address> response = addressController.updateAddress(request);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedAddress, response.getBody());
    }

    @Test
    void updateAddress_shouldThrowException_whenAddressDoesNotExist() {
        // Arrange
        ReqUpdateAddressDto request = new ReqUpdateAddressDto();
        request.setId(1L);

        when(addressService.updateAddress(request)).thenReturn(null);

        // Act & Assert
        IdInvalidException exception = assertThrows(
                IdInvalidException.class,
                () -> addressController.updateAddress(request)
        );

        assertEquals("Address with id 1 does not exist", exception.getMessage());
    }
}
