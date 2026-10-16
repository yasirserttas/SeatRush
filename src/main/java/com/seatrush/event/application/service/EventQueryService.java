package com.seatrush.event.application.service;

import com.seatrush.event.application.port.in.EventQueryUseCase;
import com.seatrush.event.application.port.out.EventQueryPort;
import com.seatrush.event.application.query.EventView;
import com.seatrush.event.domain.exception.EventNotFoundException;

import java.util.List;
import java.util.UUID;

public class EventQueryService implements EventQueryUseCase {

    private final EventQueryPort eventQueryPort;

    public EventQueryService(EventQueryPort eventQueryPort) {
        this.eventQueryPort = eventQueryPort;
    }

    @Override
    public EventView getById(UUID id) {
        return eventQueryPort.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));
    }

    @Override
    public EventView getByTitle(String title) {
        return eventQueryPort.findByTitle(title)
                .orElseThrow(() -> new EventNotFoundException(title));
    }

    @Override
    public List<EventView> getAll() {
        return eventQueryPort.findAll();
    }
}
