package com.seatrush.seat.application.port.out;

import com.seatrush.seat.domain.model.Seat;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SeatRepository {
    Seat save(Seat seat);
    void saveAll(List<Seat> seats);
    Optional<Seat> findById(UUID id);
    Optional<Seat> findByIdAndEventId(UUID id, UUID eventId);
    List<Seat> findByEventId(UUID eventId);
    void delete(UUID id);
    Optional<Seat> findByIdWithLock(UUID id);
}
