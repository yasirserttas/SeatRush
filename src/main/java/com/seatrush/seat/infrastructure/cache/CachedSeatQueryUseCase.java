package com.seatrush.seat.infrastructure.cache;

import com.seatrush.seat.application.port.in.SeatQueryUseCase;
import com.seatrush.seat.application.query.SeatView;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class CachedSeatQueryUseCase implements SeatQueryUseCase {

    private final SeatQueryUseCase delegate;

    @Override
    public SeatView getById(UUID id) {
        return delegate.getById(id);
    }

    @Override
    public SeatView getByIdAndEventId(UUID id, UUID eventId) {
        return delegate.getByIdAndEventId(id, eventId);
    }

    @Override
    @Cacheable(value = "eventSeats", key = "#id")
    public List<SeatView> getByEventId(UUID id) {
        return delegate.getByEventId(id);
    }

    @Override
    public List<SeatView> getAll() {
        return delegate.getAll();
    }
}
