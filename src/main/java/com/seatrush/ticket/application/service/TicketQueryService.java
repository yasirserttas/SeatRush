package com.seatrush.ticket.application.service;

import com.seatrush.ticket.application.port.in.TicketQueryUseCase;
import com.seatrush.ticket.application.port.out.TicketQueryPort;
import com.seatrush.ticket.application.query.TicketView;
import com.seatrush.ticket.domain.exception.TicketNotFoundException;

import java.util.List;
import java.util.UUID;

public class TicketQueryService implements TicketQueryUseCase {

    private final TicketQueryPort ticketQueryPort;


    public TicketQueryService(TicketQueryPort ticketQueryPort) {
        this.ticketQueryPort = ticketQueryPort;
    }

    @Override
    public TicketView findById(UUID id) {
        return ticketQueryPort.findById(id)
                .orElseThrow(() -> new TicketNotFoundException(id));
    }

    @Override
    public TicketView findByTicketCode(String ticketCode) {
        return ticketQueryPort.findByTicketCode(ticketCode)
                .orElseThrow(() -> new TicketNotFoundException(ticketCode));
    }

    @Override
    public List<TicketView> findByUserId(UUID userId) {
        return ticketQueryPort.findByUserId(userId);
    }
}
