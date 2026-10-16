package com.seatrush.event;

import java.util.UUID;

public record EventCreated(
        UUID eventId,
        int totalCapacity
) {
}