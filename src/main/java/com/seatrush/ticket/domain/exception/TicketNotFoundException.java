package com.seatrush.ticket.domain.exception;

import java.util.UUID;

public class TicketNotFoundException extends TicketDomainException {

    public TicketNotFoundException(UUID id) {
        super("Ticket not found with id: " + id);
    }

    public TicketNotFoundException(String ticketCode) {
        super("Ticket not found with code: " + ticketCode);
    }
}
