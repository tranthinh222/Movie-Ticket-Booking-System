package com.cinema.ticketbooking.util.error;

public class UnavailableResourceException extends RuntimeException {
    public UnavailableResourceException(String message) {
        super(message);
    }
}
