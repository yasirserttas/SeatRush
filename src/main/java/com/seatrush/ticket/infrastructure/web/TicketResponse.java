package com.seatrush.ticket.infrastructure.web;

import com.seatrush.ticket.domain.model.TicketStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record TicketResponse(
        UUID id,
        String ticketCode,
        UUID userId,
        UUID seatId,
        UUID eventId,
        LocalDateTime issuedAt,
        TicketStatus ticketStatus,
        LocalDateTime usedAt,
        LocalDateTime cancelledAt
) {
}
