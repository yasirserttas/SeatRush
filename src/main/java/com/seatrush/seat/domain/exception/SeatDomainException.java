package com.seatrush.seat.domain.exception;

public class SeatDomainException extends RuntimeException {

    public SeatDomainException(String message) {
        super(message);
    }

    public SeatDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
