package com.cinebook.booking.exception;

import org.springframework.http.HttpStatus;

/** Thrown when the seat_lock insert fails because another request grabbed the seat first. */
public class SeatConflictException extends ApiException {
    public SeatConflictException(String seatNo) {
        super("Sorry! Seat " + seatNo + " was just booked by someone else. Please select different seats.", HttpStatus.CONFLICT);
    }
}
