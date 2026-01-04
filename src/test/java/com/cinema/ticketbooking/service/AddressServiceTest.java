package com.cinema.ticketbooking.service;

import com.cinema.ticketbooking.domain.Address;
import com.cinema.ticketbooking.domain.request.ReqCreateAddressDto;
import com.cinema.ticketbooking.domain.request.ReqUpdateAddressDto;
import com.cinema.ticketbooking.domain.response.ResultPaginationDto;
import com.cinema.ticketbooking.repository.AddressRepository;
import com.cinema.ticketbooking.repository.projection.AddressWithTheatersProjection;
import com.cinema.ticketbooking.repository.projection.TheaterIdNameProjection;
import com.cinema.ticketbooking.util.error.BadRequestException;
import com.cinema.ticketbooking.util.error.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;

    @InjectMocks
    private AddressService addressService;

    @Test
    void getAllAddresses_shouldReturnResultPaginationDto_whenCalled() {
        // Arrange
        Address address = new Address();
        List<Address> content = List.of(address);
        Page<Address> page = new PageImpl<>(content, PageRequest.of(0, 10), 1);
        when(addressRepository.findAll(
                ArgumentMatchers.<Specification<Address>>any(),
                any(Pageable.class)
        )).thenReturn(page);

        Pageable pageable = PageRequest.of(0, 10);

        // Act
        ResultPaginationDto result = addressService.getAllAddresses(null, pageable);

        // Assert
        assertNotNull(result.getMeta());
        assertEquals(content, result.getData());
        assertEquals(1, result.getMeta().getTotalItems());
    }

    @Test
    void createAddress_shouldReturnAddress_whenCalled() {
        // Arrange
        ReqCreateAddressDto req = new ReqCreateAddressDto();
        req.setCity("Hanoi");
        req.setStreet_name("Street A");
        req.setStreet_number("123");

        // Act
        Address result = addressService.createAddress(req);

        // Assert
        assertEquals("Hanoi", result.getCity());
        assertEquals("Street A", result.getStreet_name());
        assertEquals("123", result.getStreet_number());
        verify(addressRepository).save(result);
    }

    @Test
    void findAddressById_shouldReturnAddress_whenFound() {
        // Arrange
        Address address = new Address();
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        // Act
        Address result = addressService.findAddressById(1L);

        // Assert
        assertEquals(address, result);
    }

    @Test
    void findAddressById_shouldReturnNull_whenNotFound() {
        // Arrange
        when(addressRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Address result = addressService.findAddressById(1L);

        // Assert
        assertNull(result);
    }

    @Test
    void deleteAddress_shouldCallRepository_whenCalled() {
        // Act
        addressService.deleteAddress(1L);

        // Assert
        verify(addressRepository).deleteById(1L);
    }

    @Test
    void updateAddress_shouldReturnUpdatedAddress_whenFieldsProvided() {
        // Arrange
        Address address = new Address();
        address.setId(1L);
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        ReqUpdateAddressDto req = new ReqUpdateAddressDto();
        req.setId(1L);
        req.setCity("New City");
        req.setStreet_name("New Street");

        // Act
        Address result = addressService.updateAddress(req);

        // Assert
        assertEquals("New City", result.getCity());
        assertEquals("New Street", result.getStreet_name());
        verify(addressRepository).save(address);
    }

    @Test
    void updateAddress_shouldThrowBadRequestException_whenNoFieldsProvided() {
        // Arrange
        ReqUpdateAddressDto req = new ReqUpdateAddressDto();
        req.setId(1L);

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> addressService.updateAddress(req)
        );
        assertEquals("No data provided for update", exception.getMessage());
    }

    @Test
    void updateAddress_shouldReturnNull_whenAddressNotFound() {
        // Arrange
        ReqUpdateAddressDto req = new ReqUpdateAddressDto();
        req.setId(1L);
        req.setCity("New City");
        when(addressRepository.findById(1L)).thenReturn(Optional.empty());

        // Act
        Address result = addressService.updateAddress(req);

        // Assert
        assertNull(result);
    }

    @Test
    void getTheatersByAddressId_shouldReturnTheaterProjections_whenAddressExists() {
        // Arrange
        TheaterIdNameProjection theaterProjection = mock(TheaterIdNameProjection.class);
        when(theaterProjection.getId()).thenReturn(1L);
        when(theaterProjection.getName()).thenReturn("Theater 1");

        AddressWithTheatersProjection addressProjection = mock(AddressWithTheatersProjection.class);
        when(addressProjection.getTheaters()).thenReturn(List.of(theaterProjection));

        when(addressRepository.findProjectedById(1L)).thenReturn(Optional.of(addressProjection));

        // Act
        List<TheaterIdNameProjection> result = addressService.getTheatersByAddressId(1L);

        // Assert
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("Theater 1", result.get(0).getName());
    }

    @Test
    void getTheatersByAddressId_shouldThrowNotFoundException_whenAddressNotFound() {
        // Arrange
        when(addressRepository.findProjectedById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> addressService.getTheatersByAddressId(1L)
        );
        assertEquals("Address with id 1 not found", exception.getMessage());
    }
}
