package com.seatrush.seat.application.command;

import java.util.UUID;

public record CreateSeatCommand(UUID eventId , int seatNumber) {
}
