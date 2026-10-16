package com.seatrush.seat.infrastructure.cache;

import com.seatrush.seat.SeatReservationApi;
import com.seatrush.seat.application.command.CreateSeatCommand;
import com.seatrush.seat.application.command.CreateSeatsForEventCommand;
import com.seatrush.seat.application.port.in.SeatCommandUseCase;
import com.seatrush.seat.application.port.out.SeatRepository;
import com.seatrush.seat.domain.model.Seat;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.transaction.TransactionAwareCacheDecorator;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class CacheEvictingSeatCommandUseCase implements SeatCommandUseCase, SeatReservationApi {

    static final String EVENT_SEATS_CACHE = "eventSeats";

    private final SeatCommandUseCase delegate;
    private final SeatRepository seatRepository;
    private final CacheManager cacheManager;

    @Override
    public UUID create(CreateSeatCommand command) {
        UUID seatId = delegate.create(command);
        evict(command.eventId());
        return seatId;
    }

    @Override
    public void createSeatsForEvent(CreateSeatsForEventCommand command) {
        delegate.createSeatsForEvent(command);
        evict(command.eventId());
    }

    @Override
    public void release(UUID id) {
        runAndEvict(id, delegate::release);
    }

    @Override
    public void reserve(UUID id) {
        runAndEvict(id, delegate::reserve);
    }

    @Override
    public void sell(UUID id) {
        runAndEvict(id, delegate::sell);
    }

    @Override
    public void delete(UUID id) {
        runAndEvict(id, delegate::delete);
    }

    private void runAndEvict(UUID seatId, Consumer<UUID> action) {
        Optional<UUID> eventId = seatRepository.findById(seatId).map(Seat::getEventId);
        action.accept(seatId);
        eventId.ifPresent(this::evict);
    }

    private void evict(UUID eventId) {
        Cache cache = cacheManager.getCache(EVENT_SEATS_CACHE);
        if (cache != null) {
            new TransactionAwareCacheDecorator(cache).evict(eventId);
        }
    }
}
