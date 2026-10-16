package com.seatrush.event.infrastructure.transaction;

import com.seatrush.event.application.port.in.EventQueryUseCase;
import com.seatrush.event.application.query.EventView;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TransactionalEventQueryUseCase implements EventQueryUseCase {

    private final EventQueryUseCase delegate;


    @Override
    public EventView getById(UUID id) {
        return delegate.getById(id);
    }

    @Override
    public EventView getByTitle(String title) {
        return delegate.getByTitle(title);
    }

    @Override
    public List<EventView> getAll() {
        return delegate.getAll();
    }
}
