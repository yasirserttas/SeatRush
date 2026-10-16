package com.seatrush.seat.infrastructure.web;

import java.util.List;
import java.util.UUID;

public record EventSeatsResponse(
        UUID eventId,
        int totalCount,
        List<SeatResponse> seats
) {
}