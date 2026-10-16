package com.seatrush.seat.application.port.out;

import com.seatrush.seat.application.query.SeatView;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SeatQueryPort {

    Optional<SeatView> findViewBySeatId(UUID seatId);
    Optional<SeatView> findViewByIdAndEventId(UUID seatId,UUID eventId);
    List<SeatView> findByEventId(UUID eventId);
    List<SeatView> findAll();
}
