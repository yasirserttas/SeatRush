package com.seatrush.ticket.application.port.out;

import com.seatrush.ticket.domain.model.Ticket;

import java.util.Optional;
import java.util.UUID;

public interface TicketRepository {
    Ticket save(Ticket ticket);
    void delete(UUID id);
    Optional<Ticket> findById(UUID id);
    Optional<Ticket> findByTicketCode(String ticketCode);
}
