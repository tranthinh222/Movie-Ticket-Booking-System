package com.cinema.ticketbooking.util.error;

public class NoResourceException extends RuntimeException {
    public NoResourceException(String message) {
        super(message);
    }
}
