package com.seatrush.ticket.infrastructure.transaction;

import com.seatrush.ticket.application.port.in.TicketQueryUseCase;
import com.seatrush.ticket.application.query.TicketView;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TransactionalTicketQueryUseCase implements TicketQueryUseCase {

    private final TicketQueryUseCase ticketQueryUseCase;

    @Override
    public TicketView findById(UUID id) {
        return ticketQueryUseCase.findById(id);
    }

    @Override
    public TicketView findByTicketCode(String ticketCode) {
        return ticketQueryUseCase.findByTicketCode(ticketCode);
    }

    @Override
    public List<TicketView> findByUserId(UUID userId) {
        return ticketQueryUseCase.findByUserId(userId);
    }
}
