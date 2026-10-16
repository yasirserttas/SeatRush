package com.seatrush.ticket.domain.exception;

public class InvalidTicketStateException extends TicketDomainException {

    public InvalidTicketStateException(String message) {
        super(message);
    }
}
