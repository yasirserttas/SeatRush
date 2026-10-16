package com.seatrush.ticket.infrastructure.transaction;

import com.seatrush.ticket.application.command.CreateTicketCommand;
import com.seatrush.ticket.application.port.in.TicketCommandUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Transactional
@RequiredArgsConstructor
public class TransactionalTicketCommandUseCase implements TicketCommandUseCase {

    private final TicketCommandUseCase ticketCommandUseCase;

    @Override
    public UUID create(CreateTicketCommand command) {
        return ticketCommandUseCase.create(command);
    }

    @Override
    public void delete(UUID id) {
        ticketCommandUseCase.delete(id);
    }

    @Override
    public void useTicket(UUID id) {
        ticketCommandUseCase.useTicket(id);
    }

    @Override
    public void cancelTicket(UUID id) {
        ticketCommandUseCase.cancelTicket(id);
    }
}
