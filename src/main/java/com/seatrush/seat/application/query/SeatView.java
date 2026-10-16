package com.seatrush.seat.application.query;

import com.seatrush.seat.domain.model.SeatStatus;

import java.io.Serializable;
import java.util.UUID;

public record SeatView(UUID id,UUID eventId , int seatNumber, SeatStatus  seatStatus) implements Serializable {
}
