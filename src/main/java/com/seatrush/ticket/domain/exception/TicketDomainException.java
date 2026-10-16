package com.seatrush.ticket.domain.exception;

public class TicketDomainException extends RuntimeException {

    public TicketDomainException(String message) {
        super(message);
    }

    public TicketDomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
