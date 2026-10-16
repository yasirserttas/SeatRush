package com.seatrush.ticket.application.command;

import java.util.UUID;

public record CreateTicketCommand(UUID userId,UUID seatId,UUID eventId) {
}
