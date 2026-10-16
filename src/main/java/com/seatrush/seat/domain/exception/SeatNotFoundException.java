package com.seatrush.seat.domain.exception;

import java.util.UUID;

public class SeatNotFoundException extends SeatDomainException {

    public SeatNotFoundException(UUID id) {
        super("Seat not found with id: " + id);
    }

    public SeatNotFoundException(UUID id, UUID eventId) {
        super("Seat not found with id: " + id + " for event: " + eventId);
    }

    public SeatNotFoundException(String message) {
        super(message);
    }
}
