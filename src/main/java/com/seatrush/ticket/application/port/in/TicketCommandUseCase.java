package com.seatrush.ticket.application.port.in;

import com.seatrush.ticket.application.command.CreateTicketCommand;

import java.util.UUID;

public interface TicketCommandUseCase {
    UUID create(CreateTicketCommand command);
    void delete(UUID id);
    void useTicket(UUID id);
    void cancelTicket(UUID id);
}
