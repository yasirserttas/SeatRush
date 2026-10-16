package com.seatrush.ticket.infrastructure.web;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateTicketRequest(
        @NotNull
        UUID userId,
        @NotNull
        UUID seatId,
        @NotNull
        UUID eventId
) {
}
