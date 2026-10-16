package com.seatrush.seat.infrastructure.web;

import com.seatrush.seat.domain.model.SeatStatus;

import java.util.UUID;

public record SeatResponse(
        UUID id,
        UUID eventId,
        int seatNumber,
        SeatStatus seatStatus
) {
}