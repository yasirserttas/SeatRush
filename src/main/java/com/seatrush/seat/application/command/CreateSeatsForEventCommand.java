package com.seatrush.seat.application.command;

import java.util.UUID;

public record CreateSeatsForEventCommand(UUID eventId, int capacity) {
}

