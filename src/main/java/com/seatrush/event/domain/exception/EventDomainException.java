package com.seatrush.event.domain.exception;

public class EventDomainException extends RuntimeException {

    public EventDomainException(String message) {
        super(message);
    }

    public EventDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
