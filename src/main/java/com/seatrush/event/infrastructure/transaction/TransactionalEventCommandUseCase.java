package com.seatrush.event.infrastructure.transaction;

import com.seatrush.event.application.command.CreateEventCommand;
import com.seatrush.event.application.command.UpdateEventCommand;
import com.seatrush.event.application.port.in.EventCommandUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Transactional
@RequiredArgsConstructor
public class TransactionalEventCommandUseCase implements EventCommandUseCase {

    private final EventCommandUseCase delegate;

    @Override
    public UUID create(CreateEventCommand command) {
        return delegate.create(command);
    }

    @Override
    public void update(UpdateEventCommand command) {
        delegate.update(command);
    }

    @Override
    public void delete(UUID id) {
        delegate.delete(id);
    }

    @Override
    public void activate(UUID eventId) {
        delegate.activate(eventId);
    }

    @Override
    public void cancel(UUID eventId) {
        delegate.cancel(eventId);
    }
}