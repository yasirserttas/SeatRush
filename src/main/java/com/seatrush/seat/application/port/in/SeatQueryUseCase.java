package com.seatrush.seat.application.port.in;

import com.seatrush.seat.application.query.SeatView;

import java.util.List;
import java.util.UUID;

public interface SeatQueryUseCase {
    SeatView getById(UUID id);
    SeatView getByIdAndEventId(UUID id, UUID eventId);
    List<SeatView> getByEventId(UUID id);
    List<SeatView> getAll();
}
