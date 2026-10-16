package com.seatrush.seat.infrastructure.transaction;

import com.seatrush.seat.SeatReservationApi;
import com.seatrush.seat.application.command.CreateSeatCommand;
import com.seatrush.seat.application.command.CreateSeatsForEventCommand;
import com.seatrush.seat.application.port.in.SeatCommandUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Transactional
@RequiredArgsConstructor
public class TransactionalSeatCommandUseCase implements SeatCommandUseCase, SeatReservationApi {

    private final SeatCommandUseCase delegate;

    @Override
    public UUID create(CreateSeatCommand command) {
        return delegate.create(command);
    }

    @Override
    public void createSeatsForEvent(CreateSeatsForEventCommand command) {
        delegate.createSeatsForEvent(command);
    }

    @Override
    public void release(UUID id) {
        delegate.release(id);
    }

    @Override
    public void reserve(UUID id) {
        delegate.reserve(id);
    }

    @Override
    public void sell(UUID id) {
        delegate.sell(id);
    }

    @Override
    public void delete(UUID id) {
        delegate.delete(id);
    }
}
