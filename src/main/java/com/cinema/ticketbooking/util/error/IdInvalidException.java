package com.cinema.ticketbooking.util.error;

public class IdInvalidException extends RuntimeException {

    // Constructor that accepts a message
    public IdInvalidException(String message) {
        super(message);
    }
}
