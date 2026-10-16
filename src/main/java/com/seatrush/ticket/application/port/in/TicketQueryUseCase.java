package com.seatrush.ticket.application.port.in;

import com.seatrush.ticket.application.query.TicketView;

import java.util.List;
import java.util.UUID;

public interface TicketQueryUseCase {
    TicketView findById(UUID id);
    TicketView findByTicketCode(String ticketCode);
    List<TicketView> findByUserId(UUID userId);

}
