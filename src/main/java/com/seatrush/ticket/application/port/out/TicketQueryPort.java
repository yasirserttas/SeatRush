package com.seatrush.ticket.application.port.out;

import com.seatrush.ticket.application.query.TicketView;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TicketQueryPort {
    Optional<TicketView> findById(UUID id);
    Optional<TicketView> findByTicketCode(String ticketCode);
    List<TicketView> findByUserId(UUID userId);
}
