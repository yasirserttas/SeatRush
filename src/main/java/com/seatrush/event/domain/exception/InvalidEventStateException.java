package com.seatrush.event.domain.exception;

public class InvalidEventStateException extends EventDomainException {

    public InvalidEventStateException(String message) {
        super(message);
    }
}
